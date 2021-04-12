package info.free.scp.util

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import info.free.scp.BuildConfig
import info.free.scp.db.AppInfoDatabase
import info.free.scp.service.HttpManager
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info


class UpdateManager(private var activity: BaseActivity) {

    init {
        manager = this
    }

    companion object {
        var manager: UpdateManager? = null

        fun getInstance(newActivity: BaseActivity): UpdateManager {
            manager?.activity = newActivity
            return manager ?: UpdateManager(newActivity)
        }
    }

    private val currentVersionCode = BuildConfig.VERSION_CODE


    /**
     * 检测更新和数据初始化
     * ---start---
     * 0. 第一次启动，进入新手引导流程，不检查更新
     * 1. 检测app版本，确保是最新版：checkUpdateAndCategory()
     * 2. 检测本地是否有备份数据，如果有，执行恢复并把初始化标记位都置为true，跳转5，否则转3
     * 3. 检测目录是否初始化，加载目录，完毕后加载正文
     * 4. 检测正文是否加载完成，没有的话继续加载
     * 5. 录已加载完的前提下，检测姓名和职位是否存在（一般是第二天打开了，提示输入姓名和职位
     * ---finish---
     */
    fun checkAppData() {
        activity.info("start checkAppData()")
        if (PreferenceUtil.isFirstInstallApp()) {
            // 显示新人引导，下次再检测更新
            PreferenceUtil.setFirstInstallApp()
            NewbieManager.showLevelDialog(activity)
            return
        }
        if (PreferenceUtil.getFirstOpenCurrentVersion(currentVersionCode.toString())) {
            // 当前版本第一次启动app，把检测更新时间重置，再检测一次更新
            PreferenceUtil.setFirstOpenCurrentVersion(currentVersionCode.toString())
            AppInfoDatabase.getInstance()
        }

        if (Utils.enabledNetwork(activity)) {
            PreferenceUtil.addPoints(1)
            Log.i("scp", "checkUpdate")
            // 记录上次检测更新时间
            // 检测app版本，如果有更新版本就不加载目录，等更新之后再加载
            getConfig()
        }
    }

    fun getConfig() {
        doAsync {
            // 检查更新信息
            var newVersionCode = 0
            var updateDesc: String? = ""
            var updateLink: String? = ""
            HttpManager.instance.getAppConfig {
                activity.info("getAppConfig result = $it")
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
                    if (config.key == "last_update_time_all") {
                        PreferenceUtil.setServerLastUpdateTime(config.value.toLong())
                    }
                    if (config.key == "notice") {
                        PreferenceUtil.setNotice(config.value)
                    }
                    if (config.key == "api_url") {
                        PreferenceUtil.setApiUrl(config.value)
                    }
                    if (config.key == "show_meal") {
                        PreferenceUtil.setShowMeal(config.value == "yes")
                    }
                    if (config.key == "meal_count") {
                        PreferenceUtil.setNewMealCount(config.value.toInt())
                    }
                }
                if (currentVersionCode < newVersionCode && !activity.isFinishing) {
                    activity.info("current = $currentVersionCode, new = $newVersionCode, 需要升级")
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
                            .setNegativeButton("暂不升级") { _, _ -> }
                            .create().show()
                    // 有新版本就不检查数据更新，知道更新到最新
                }
            }
        }
    }
}