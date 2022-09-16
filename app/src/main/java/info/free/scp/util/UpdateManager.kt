package info.free.scp.util

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import info.free.scp.BuildConfig
import info.free.scp.ScpApplication.Companion.database
import info.free.scp.db.AppInfoDatabase
import info.free.scp.view.base.BaseActivity
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
            // 更新category数据库
//            FileUtil.copyCategoryDb(true)
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

    private fun getConfig() {
        // Write a message to the database
        database.child("config").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            // 检查更新信息
            var newVersionCode = 0
            var updateDesc: String? = ""
            var updateLink: String? = ""
            newVersionCode = it.child("app_version").getValue<Int>() ?: 0
            updateDesc = it.child("update_desc").getValue<String>()
            updateLink = it.child("update_link").getValue<String>()
            PreferenceUtil.setNotice(it.child("notice").getValue<String>() ?: "")
            PreferenceUtil.setApiUrl(it.child("api_url").getValue<String>() ?: "")
            PreferenceUtil.setQueryLink(it.child("query_link").getValue<String>() ?: "")
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
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }
}