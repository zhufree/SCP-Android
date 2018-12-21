package info.free.scp.util

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import info.free.scp.BuildConfig
import info.free.scp.SCPConstants
import info.free.scp.db.ScpDao
import info.free.scp.service.HttpManager
import info.free.scp.service.InitCategoryService
import info.free.scp.service.InitDetailService
import info.free.scp.view.base.BaseActivity

object UpdateManager {
    private val currentVersionCode = BuildConfig.VERSION_CODE
    private var isDownloadingDetail = false

    private var mInitCategoryReceiver : BroadcastReceiver? = null
    private var mLocalBroadcastManager: LocalBroadcastManager? = null
    private fun registerBroadCastReceivers(activity: BaseActivity) {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SCPConstants.BroadCastAction.INIT_PROGRESS)
        mLocalBroadcastManager?.registerReceiver(mInitCategoryReceiver!!, IntentFilter(SCPConstants.BroadCastAction.INIT_PROGRESS))
//        mLocalBroadcastManager?.registerReceiver(mDetailReceiver, IntentFilter(SCPConstants.BroadCastAction.LOAD_DETAIL_FINISH))
//        mLocalBroadcastManager?.registerReceiver(themeReceiver, IntentFilter(SCPConstants.BroadCastAction.ACTION_CHANGE_THEME))
    }

    fun initReceiver(activity: BaseActivity) {
        mInitCategoryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val progress = intent?.getIntExtra("progress", 0) ?: 0
                progressDialog?.progress = progress
                if (progress > 90) {
                    progressDialog?.setMessage("写入数据库中")
                }
                if (progress == 100) {
                    progressDialog?.dismiss()
                    if (!activity.isFinishing) {
                        val dbList = arrayOf("SCP系列1-5000","SCP-CN系列1-2000","其他文档（解明，废除，删除，归档等）",
                                "基金会故事+设定中心","相关材料")
                        val chooseList =  arrayOf(false, false,false,false,false).toBooleanArray()
                        // TODO 显示分库下载选项
                        AlertDialog.Builder(activity)
                                .setTitle("Notice")
                                .setMultiChoiceItems(dbList, chooseList){ out, which, isChecked ->
                                    chooseList[which] = isChecked
//                                    AlertDialog.Builder(activity)
//                                            .setTitle(dbList[which])
//                                            .setMessage(PreferenceUtil.getDescForJob(dbList[which]))
//                                            .setPositiveButton("确定") { inner, _ ->
//
//                                                inner.dismiss()
//                                                field?.set(out, true)
//                                                out.dismiss()
//                                            }
//                                            .setNegativeButton("我手滑了") { dialog, _ -> dialog.dismiss() }
//                                            .create().show()

                                }
                                .setPositiveButton("OK") { dialog, _ ->
                                    // TODO 下载对应数据库
                                }
                                .create().show()
                    }
                }
            }
        }
        registerBroadCastReceivers(activity)
    }

    fun checkAppData(activity: BaseActivity) {
        // 第一次启动app，把检测更新时间重置，再检测一次更新
        if (PreferenceUtil.getFirstOpenCurrentVersion(currentVersionCode.toString())) {
            PreferenceUtil.setLastCheckUpdateTime(0)
            PreferenceUtil.setFirstOpenCurrentVersion(currentVersionCode.toString())
        } else if (!isDownloadingDetail) {
            // 如果没有在下载正文，检测正文是否下完
            checkInitDetailData(activity)
        }
        // 普通情况下一天检测一次更新
        if (PreferenceUtil.checkNeedShowUpdateNotice() && Utils.enabledNetwork(activity)) {
            PreferenceUtil.addPoints(1)
            Log.i("scp", "checkUpdate()")
            PreferenceUtil.setLastCheckUpdateTime(System.currentTimeMillis())
            // 检测app版本，如果有更新版本就不加载目录，等更新之后再加载
            checkUpdateAndCategory(activity)
//            checkUserInfo()
        }
    }

    /**
     * 检查更新信息
     */
    private fun checkUpdateAndCategory(activity: BaseActivity) {
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
                // 上次数据更新时间，分几个库
                if (config.key == "db_1_last_update_time") {
                    PreferenceUtil.setServerLastUpdateTime(config.key, config.value)
                }
            }
            if (currentVersionCode < newVersionCode) {
                Log.i("version", "current = $currentVersionCode, new = $newVersionCode, 需要升级")
                AlertDialog.Builder(activity)
                        .setTitle("发现新版本")
                        .setMessage(updateDesc)
                        .setPositiveButton("现在升级") { _, _ ->
                            val updateIntent = Intent()
                            updateIntent.action = "android.intent.action.VIEW"
                            val updateUrl = Uri.parse(updateLink)
                            updateIntent.data = updateUrl
                            activity.startActivity(updateIntent)
                        }
                        .setNegativeButton("暂不升级") { dialog, _ -> dialog.dismiss() }
                        .create().show()
                return@getAppConfig
            }
            // 显示更新就不检查数据，更新完毕后再检测数据更新
            checkInitData(activity, false)
        }
    }

    /**
     * 数据初始化入口
     * 1.为了防止与后台数据库检测自动更新同时调用导致数据加载重复，确定没有版本更新才检测数据更新，否则先更新版本
     * 2.点击按钮手动检测时调用
     * [forceInit] 强制更新，不强制更新的话可以直接使用备份数据
     */
    fun checkInitData(activity: BaseActivity, forceInit: Boolean = false) {
        if (!forceInit && BackupHelper.getInstance(activity).checkBackUpFileExist()) {
            if (BackupHelper.getInstance(activity).restore()) {
                PreferenceUtil.setInitCategoryFinish(true)
                PreferenceUtil.setDetailDataLoadFinish()
                return
            }
        }
        if (!PreferenceUtil.getInitCategoryFinish()) {
            // 目录和正文都没加载
            if (Utils.enabledNetwork(activity)) {
                ScpDao.getInstance().resetDb()
                initCategoryData(activity)
                if (Utils.enabledWifi(activity)) {
//                    initDetailData(activity)
                } else if (Utils.enabledNetwork(activity)) {
                    AlertDialog.Builder(activity)
                            .setTitle("数据初始化")
                            .setMessage("检测到你没有开启wifi，是否允许请求网络加载正文数据（可能消耗上百M流量）？")
                            .setPositiveButton("确定") { _, _ ->
//                                initDetailData(activity)
                            }
                            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                            .create().show()
                }
            } else {
                AlertDialog.Builder(activity)
                        .setTitle("数据初始化")
                        .setMessage("检测到你没有开启网络，请手动开启网络后在【其他】页面选择同步云端数据" +
                                "（本次初始化完成后到下次数据更新之间不需要再加载目录信息）")
                        .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            }
        } else {
            // 目录加载了，检测正文是否下完
//            checkInitDetailData(activity)
        }
    }

    var progressDialog: ProgressDialog? = null
    /**
     * 初始化数据
     */
    private fun initCategoryData(activity: BaseActivity) {
        val intent = Intent(activity, InitCategoryService::class.java)
        activity.startService(intent)
        progressDialog = ProgressDialog(activity)
        progressDialog?.max = 100
        progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog?.setMessage("与基金会通信中")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun initDetailData(activity: BaseActivity) {
        val intent = Intent(activity, InitDetailService::class.java)
        activity.startService(intent)
        isDownloadingDetail = true
    }

    private fun checkInitDetailData(activity: BaseActivity) {
        if (!PreferenceUtil.getDetailDataLoadFinish()) {
            // 正文没有加载完
            if (Utils.enabledWifi(activity)) {
//                initDetailData(activity)
            } else if (Utils.enabledNetwork(activity)) {
                AlertDialog.Builder(activity)
                        .setTitle("数据初始化")
                        .setMessage("检测到你没有开启wifi，是否允许请求网络加载正文数据（可能消耗上百M流量）？")
                        .setPositiveButton("确定") { _, _ ->
//                            initDetailData(activity)
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            }
            // 如果没网就不管了，下次再下
        }
    }
}