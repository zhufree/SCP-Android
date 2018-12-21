package info.free.scp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import info.free.scp.SCPConstants.BroadCastAction.ACTION_CHANGE_THEME
import info.free.scp.SCPConstants.BroadCastAction.INIT_PROGRESS
import info.free.scp.SCPConstants.BroadCastAction.LOAD_DETAIL_FINISH
import info.free.scp.db.ScpDao
import info.free.scp.service.HttpManager
import info.free.scp.service.InitCategoryService
import info.free.scp.service.InitDetailService
import info.free.scp.util.*
import info.free.scp.util.EventUtil.chooseJob
import info.free.scp.view.user.UserFragment
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.ScpListActivity
import info.free.scp.view.home.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_dialog_report.view.*


class MainActivity : BaseActivity(), HomeFragment.CategoryListener, UserFragment.AboutListener {
    private var currentFragment: BaseFragment? = null
    private val homeFragment = HomeFragment.newInstance()
//    private val feedFragment = FeedFragment.newInstance()
    private val userFragment = UserFragment.newInstance()
    private var remoteDbVersion = -1
    private var isDownloadingDetail = false

    var progressDialog: ProgressDialog? = null
    private val currentVersionCode = BuildConfig.VERSION_CODE

    private var mLocalBroadcastManager: LocalBroadcastManager? = null
    private var mInitCategoryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra("progress", 0) ?: 0
            progressDialog?.progress = progress
            if (progress > 90) {
                progressDialog?.setMessage("写入数据库中")
            }
            if (progress == 100) {
                progressDialog?.dismiss()
                if (!isFinishing) {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("Notice")
                            .setMessage("基金会传递的目录信息已接收完毕，将在后台加载正文数据，全部传输完成之前" +
                                    "将有部分文档不能离线查看，会自动显示网页模式。具体加载进度可查看通知栏，如果" +
                                    "因为网络问题加载卡住等可尝试退出重进app或在设置页【同步云端数据】。")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create().show()
                }
            }
        }
    }

    private var mDetailReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Toaster.show("正文已全部传输完毕")
            isDownloadingDetail = false
            PreferenceUtil.setLastUpdateDbTime()
            BackupHelper.getInstance(this@MainActivity).backupDB()
        }
    }

    private var themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshTheme()
        }
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val transaction = supportFragmentManager?.beginTransaction()
        transaction?.hide(currentFragment as Fragment)
        when (item.itemId) {
            R.id.navigation_home -> {
                if (homeFragment.isAdded) {
                    transaction?.show(homeFragment)
                } else {
                    transaction?.add(R.id.flMainContainer, homeFragment)
                }
                currentFragment = homeFragment
            }
        //            R.id.navigation_feed -> {
        //                if (feedFragment.isAdded) {
        //                    transaction.show(feedFragment)
        //                } else {
        //                    transaction.add(R.id.flMainContainer, feedFragment)
        //                }
        //                currentFragment = feedFragment
        //                return@OnNavigationItemSelectedListener true
        //            }
            R.id.navigation_about -> {
                if (userFragment.isAdded) {
                    transaction?.show(userFragment)
                } else {
                    transaction?.add(R.id.flMainContainer, userFragment)
                }
                currentFragment = userFragment
            }
        }
        transaction?.commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注册广播
//        registerBroadCastReceivers()

        setContentView(R.layout.activity_main)
        // 恢复意外退出的数据（不知道有没有用）
        savedInstanceState?.let {
            currentFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment") as BaseFragment
        }

        // 设置默认fragment
        val transaction = supportFragmentManager?.beginTransaction()
        if (currentFragment != null) {
            if (currentFragment?.isAdded == true) {
                transaction?.show(currentFragment!!)
            } else {
                transaction?.add(R.id.flMainContainer, currentFragment!!)
            }
        } else {
            transaction?.add(R.id.flMainContainer, homeFragment)
            currentFragment = homeFragment
        }
        transaction?.commit()
        navigation?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        checkAppData()
    }

    /**
     * 注册广播
     * 1. 初始化目录
     * 2. 加载正文
     * 3. 主题
     */
    private fun registerBroadCastReceivers() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(INIT_PROGRESS)
        mLocalBroadcastManager?.registerReceiver(mInitCategoryReceiver, intentFilter)
        mLocalBroadcastManager?.registerReceiver(mDetailReceiver, IntentFilter(LOAD_DETAIL_FINISH))
        mLocalBroadcastManager?.registerReceiver(themeReceiver, IntentFilter(ACTION_CHANGE_THEME))
    }

    /**
     * 检测更新和数据初始化
     * ---start---
     * 1. 检测app版本，确保是最新版
     * 2. 检测本地是否有备份数据，如果有，执行恢复并把初始化标记位都置为true，跳转5，否则转3
     * 3. 检测目录是否初始化，加载目录，完毕后加载正文
     * 4. 检测正文是否加载完成，没有的话继续加载
     * 5. 录已加载完的前提下，检测姓名和职位是否存在（一般是第二天打开了，提示输入姓名和职位
     * ---finish---
     */
    private fun checkAppData() {
        // 第一次启动app，把检测更新时间重置，再检测一次更新
        if (PreferenceUtil.getFirstOpenCurrentVersion(currentVersionCode.toString())) {
            PreferenceUtil.setLastCheckUpdateTime(0)
            PreferenceUtil.setFirstOpenCurrentVersion(currentVersionCode.toString())
            if (PreferenceUtil.getDetailDataLoadCount() > 31) {
                PreferenceUtil.setInitCategoryFinish(true)
                PreferenceUtil.setDetailDataLoadFinish()
                BackupHelper.getInstance(this).backUp()
            }
        } else if (!isDownloadingDetail) {
            // 该版本第一次启动
            checkInitDetailData()
        }
        // 普通情况下一天检测一次更新
        if (PreferenceUtil.checkNeedShowUpdateNotice() && enabledNetwork()) {
            PreferenceUtil.addPoints(1)
            Log.i("scp", "checkUpdate()")
            PreferenceUtil.setLastCheckUpdateTime(System.currentTimeMillis())
            // 检测app版本，如果有更新版本就不加载目录，等更新之后再加载
            checkUpdateAndCategory()
            checkUserInfo()
        }
    }


    private fun checkUserInfo() {
        if (PreferenceUtil.getInitCategoryFinish() && PreferenceUtil.getNickname().isEmpty()) {
            val inputView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_report, null)
            val nameInputDialog = AlertDialog.Builder(this)
                    .setTitle("欢迎来到SCP基金会，调查员，请输入你的名字")
                    .setView(inputView)
                    .setPositiveButton("OK") { _, _ -> }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .create()
            nameInputDialog.show()
            nameInputDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                PreferenceUtil.saveNickname(inputView.et_report.text.toString())
                nameInputDialog.dismiss()
                checkJob()
            }
        }
        checkJob()
    }

    private fun checkJob() {
        if (PreferenceUtil.getNickname().isNotEmpty() && PreferenceUtil.getJob().isEmpty()) {
            val jobList = arrayOf("收容专家","研究员","安全人员","战术反应人员","外勤特工","机动特遣队作业员")
            AlertDialog.Builder(this)
                    .setTitle("欢迎来到SCP基金会，${PreferenceUtil.getNickname()}，请选择你的职业")
                    .setItems(jobList){ out, which ->
                        val field = out?.javaClass?.superclass?.getDeclaredField(
                                        "mShowing")
                        field?.isAccessible = true
                        //   将mShowing变量设为false，表示对话框已关闭
                        field?.set(out, false)
                        AlertDialog.Builder(this)
                                .setTitle(jobList[which])
                                .setMessage(PreferenceUtil.getDescForJob(jobList[which]))
                                .setPositiveButton("确定选择（暂时不可更改）") { inner, _ ->
                                    PreferenceUtil.setJob(jobList[which])
                                    EventUtil.onEvent(this, chooseJob, jobList[which])
                                    inner.dismiss()
                                    field?.set(out, true)
                                    out.dismiss()
                                }
                                .setNegativeButton("我手滑了") { dialog, _ -> dialog.dismiss() }
                                .create().show()
                    }
                    .create().show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        currentFragment?.let {
            supportFragmentManager.putFragment(outState, "currentFragment", it)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * 数据初始化入口
     * 1.为了防止与后台数据库检测自动更新同时调用导致数据加载重复，确定没有版本更新才检测数据更新，否则先更新版本
     * 2.点击按钮手动检测时调用
     * [forceInit] 强制更新，不强制更新的话可以直接使用备份数据
     */
    private fun checkInitData(forceInit: Boolean = false) {
        if (!forceInit && BackupHelper.getInstance(this).checkBackUpFileExist()) {
            if (BackupHelper.getInstance(this).restore()) {
                PreferenceUtil.setInitCategoryFinish(true)
                PreferenceUtil.setDetailDataLoadFinish()
                return
            }
        }
        if (!PreferenceUtil.getInitCategoryFinish()) {
            // 目录和正文都没加载
            if (enabledNetwork()) {
                ScpDao.getInstance().resetDb()
                initCategoryData()
                if (enabledWifi()) {
                    initDetailData()
                } else if (enabledNetwork()) {
                    AlertDialog.Builder(this)
                            .setTitle("数据初始化")
                            .setMessage("检测到你没有开启wifi，是否允许请求网络加载正文数据（可能消耗上百M流量）？")
                            .setPositiveButton("确定") { _, _ ->
                                initDetailData()
                            }
                            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                            .create().show()
                }
            } else {
                AlertDialog.Builder(this)
                        .setTitle("数据初始化")
                        .setMessage("检测到你没有开启网络，请手动开启网络后在【其他】页面选择同步云端数据" +
                                "（本次初始化完成后到下次数据更新之间不需要再加载目录信息）")
                        .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            }
        } else {
            // 目录加载了，检测正文是否下完
            checkInitDetailData()
        }

    }

    private fun checkInitDetailData() {
        if (!PreferenceUtil.getDetailDataLoadFinish()) {
            // 正文没有加载完
            if (enabledWifi()) {
                initDetailData()
            } else if (enabledNetwork()) {
                AlertDialog.Builder(this)
                        .setTitle("数据初始化")
                        .setMessage("检测到你没有开启wifi，是否允许请求网络加载正文数据（可能消耗上百M流量）？")
                        .setPositiveButton("确定") { _, _ ->
                            initDetailData()
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            }
            // 如果没网就不管了，下次再下
        }
    }

    /**
     * 初始化数据
     */
    private fun initCategoryData() {
        val intent = Intent(this, InitCategoryService::class.java)
        startService(intent)
        progressDialog = ProgressDialog(this)
        progressDialog?.max = 100
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog?.setMessage("与基金会通信中")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun initDetailData() {
        val intent = Intent(this, InitDetailService::class.java)
        startService(intent)
        isDownloadingDetail = true
    }

    /**
     * 检查更新信息
     */
    private fun checkUpdateAndCategory() {
        var newVersionCode = 0
        var updateDesc: String? = ""
        var updateLink: String? = ""
        HttpManager.instance.getAppConfig {
            for (config in it) {
                if (config.key == "version") {
                    newVersionCode = config.value.toInt()
                }
                if (config.key == "update_desc") {
                    updateDesc = config.value
                }
                if (config.key == "update_link") {
                    updateLink = config.value
                }
                // 数据版本，变化时需要重新初始化数据
                if (config.key == "db_version") {
                    remoteDbVersion = config.value.toInt()
                }
                // 上次数据更新时间
                if (config.key == "db_last_update_time") {
                    PreferenceUtil.setServerLastUpdateTime(config.key, config.value)
                }
            }
            if (currentVersionCode < newVersionCode) {
                Log.i("version", "current = $currentVersionCode, new = $newVersionCode, 需要升级")
                AlertDialog.Builder(this)
                        .setTitle("发现新版本")
                        .setMessage(updateDesc)
                        .setPositiveButton("现在升级") { _, _ ->
                            val updateIntent = Intent()
                            updateIntent.action = "android.intent.action.VIEW"
                            val updateUrl = Uri.parse(updateLink)
                            updateIntent.data = updateUrl
                            startActivity(updateIntent)
                        }
                        .setNegativeButton("暂不升级") { dialog, _ -> dialog.dismiss() }
                        .create().show()
                return@getAppConfig
            }
            // 显示更新就不检查数据，更新完毕后再检测数据更新
            checkInitData(false)
        }
    }

    fun refreshTheme() {
        navigation.setBackgroundColor(ThemeUtil.containerBg)
        homeFragment.refreshTheme()
        userFragment.refreshTheme()
    }

    override fun onCategoryClick(type: Int) {
        val intent = Intent()
        intent.putExtra("saveType", type)
        intent.setClass(this, ScpListActivity::class.java)
        startActivity(intent)
    }

    override fun onInitDataClick() {
        UpdateManager.checkInitData(this, false)
    }

    override fun onResetDataClick() {
        // 清空数据库
        ScpDao.getInstance().resetDb()
        // 重新加载
        UpdateManager.initReceiver(this)
        UpdateManager.checkInitData(this, true)
    }
}
