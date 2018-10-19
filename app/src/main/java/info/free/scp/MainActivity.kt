package info.free.scp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import info.free.scp.service.HttpManager
import info.free.scp.service.InitCategoryService
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.CategoryActivity
import info.free.scp.view.feed.FeedFragment
import info.free.scp.view.home.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.content.LocalBroadcastManager
import android.content.IntentFilter
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_SHORT
import info.free.scp.db.ScpDao
import info.free.scp.service.InitDetailService
import info.free.scp.util.Toaster


class MainActivity : BaseActivity(), HomeFragment.CategoryListener, AboutFragment.AboutListener {
    private var currentFragment: BaseFragment? = null
    private val homeFragment = HomeFragment.newInstance()
    private val feedFragment = FeedFragment.newInstance()
    private val aboutFragment = AboutFragment.newInstance()
    private var updateChecked = false
    var remoteDbVersion = -1

    var progressDialog: ProgressDialog? = null

    private var mLocalBroadcastManager: LocalBroadcastManager? = null
    private var mInitCategoryReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra("progress", 0) ?: 0
            progressDialog?.progress = progress
            if (progress > 80) {
                progressDialog?.setMessage("写入数据库中")
            }
            if (progress == 100) {
                progressDialog?.dismiss()
                AlertDialog.Builder(this@MainActivity)
                        .setTitle("Notice")
                        .setMessage("基金会传递的目录信息已接收完毕，由于正文大小较大，传输过程可能会持续很长影响" +
                                "阅读体验，因此将在使用过程中在后台加载（且仅在wifi打开时），具体进度可查看通知栏，" +
                                "在所有数据传输完毕之后将可以离线浏览所有目录下的正文。\n加载过程请尽量将app保持" +
                                "开启状态，同时手机至少保留120M存储空间。")
                        .setPositiveButton("OK") { dialog, _ ->dialog.dismiss() }
                        .create().show()
            }
        }
    }

    private var mDetailReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Toaster.show("正文已全部传输完毕！")
            val tempUpdateDbVersion = PreferenceUtil.getTempUpdateDbVersion()
            if (tempUpdateDbVersion > 0) {
                PreferenceUtil.setLocalDbVersion(tempUpdateDbVersion)
                PreferenceUtil.setTempUpdateDbVersion(-1)
            }
        }
    }

    val INIT_PROGRESS = "initProgress"
    val LOAD_DETAIL_FINISH = "loadDetailFinish"
    val currentVersionCode = BuildConfig.VERSION_CODE

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val transaction = fragmentManager.beginTransaction()
        transaction?.hide(currentFragment)
        when (item.itemId) {
            R.id.navigation_home -> {
                if (homeFragment.isAdded) {
                    transaction.show(homeFragment)
                } else {
                    transaction.add(R.id.flMainContainer, homeFragment)
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
                if (aboutFragment.isAdded) {
                    transaction.show(aboutFragment)
                } else {
                    transaction.add(R.id.flMainContainer, aboutFragment)
                }
                currentFragment = aboutFragment
            }
        }
        transaction?.commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(INIT_PROGRESS)
        mLocalBroadcastManager?.registerReceiver(mInitCategoryReceiver, intentFilter)
        mLocalBroadcastManager?.registerReceiver(mDetailReceiver, IntentFilter(LOAD_DETAIL_FINISH))

        setContentView(R.layout.activity_main)
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.flMainContainer, homeFragment)
        currentFragment = homeFragment
        transaction.commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onResume() {
        super.onResume()
        if (!updateChecked && enabledNetwork()) {
            updateChecked = true
            checkUpdate()
        }
    }

    /**
     * 数据初始化入口
     * 1.为了防止与后台数据库检测自动更新同时调用导致数据加载重复，确定没有版本更新才检测数据更新，否则先更新版本
     * 2.点击按钮手动检测时调用
     */
    private fun checkInitData() {
        if (!PreferenceUtil.getInitDataFinish()) {
            // 目录和正文都没加载
            if (enabledNetwork()){
                ScpDao.getInstance().resetDb()
                initCategoryData()
                if (enabledWifi()) {
                    initDetailData()
                } else if (enabledNetwork()){
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
                        .setMessage("检测到你没有开启网络，请手动开启网络后在【其他】页面选择初始化数据" +
                                "（本次初始化完成后到下次数据更新之间不需要再加载目录信息）")
                        .setPositiveButton("确定") { dialog, _ -> dialog.dismiss()}
                        .create().show()
            }
        } else if (PreferenceUtil.getDetailDataLoadCount() < 29) {
            // 正文没有加载完
            if (enabledWifi()) {
                initDetailData()
            } else if (enabledNetwork()){
                AlertDialog.Builder(this)
                        .setTitle("数据初始化")
                        .setMessage("检测到你没有开启wifi，是否允许请求网络加载正文数据（可能消耗上百M流量）？")
                        .setPositiveButton("确定") { _, _ ->
                            initDetailData()
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            } else {
                AlertDialog.Builder(this)
                        .setTitle("数据初始化")
                        .setMessage("检测到你没有开启网络，请手动开启网络后在【其他】页面选择初始化数据" +
                                "（本次初始化完成后到下次数据更新之间不需要再加载目录信息）")
                        .setPositiveButton("确定") { dialog, _ -> dialog.dismiss()}
                        .create().show()
            }
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
    }

    /**
     * 检查更新信息
     */
    private fun checkUpdate() {
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
            // 数据库更新了，有可能上次更新没有完成用户就退出了，用temp version判断一下
            if (remoteDbVersion > PreferenceUtil.getLocalDbVersion()
                    && remoteDbVersion > PreferenceUtil.getTempUpdateDbVersion()) {
                Log.i("db", "remote = $remoteDbVersion")
                PreferenceUtil.setTempUpdateDbVersion(remoteDbVersion)
                // 标记位初始化
                PreferenceUtil.setInitDataFinish(false)
                PreferenceUtil.resetDetailDataLoadCount()
                // 清空数据库
                ScpDao.getInstance().resetDb()
                // 重新加载
                initCategoryData()
                initDetailData()
            } else {
                checkInitData()
            }
        }
    }

    override fun onCategoryClick(type: Int) {
        val intent = Intent()
        intent.putExtra("saveType", type)
        intent.setClass(this, CategoryActivity::class.java)
        startActivity(intent)
    }

    override fun onInitDataClick() {
        checkInitData()
    }

    override fun onResetDataClick() {
        // 标记位初始化
        PreferenceUtil.setInitDataFinish(false)
        PreferenceUtil.resetDetailDataLoadCount()
        // 清空数据库
        ScpDao.getInstance().resetDb()
        // 重新加载
        initCategoryData()
        initDetailData()
    }
}
