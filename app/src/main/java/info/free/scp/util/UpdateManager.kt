package info.free.scp.util

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.database.getValue
import info.free.scp.ScpApplication
import kotlinx.coroutines.launch
import info.free.scp.BuildConfig
import info.free.scp.ScpApplication.Companion.database
import info.free.scp.db.AppInfoDatabase
import info.free.scp.view.base.BaseActivity


class UpdateManager(private var activity: BaseActivity) {

    init {
        manager = this
    }

    companion object {
        var manager: UpdateManager? = null

        internal fun confirmCatalogDownload(host: BaseActivity) {
            ScpDialogs.builder(host)
                .setTitle("下载目录更新")
                .setMessage("当前网络可能产生流量费用。是否下载目录库？")
                .setPositiveButton("下载") { _, _ -> ScpApplication.dataUpdates.downloadCatalog() }
                .setNegativeButton("取消", null)
                .show()
        }

        fun getInstance(newActivity: BaseActivity): UpdateManager {
            manager?.activity = newActivity
            return manager ?: UpdateManager(newActivity)
        }
    }

    private val currentVersionCode = BuildConfig.VERSION_CODE


    /** Checks the data release independently of Firebase app version notices and onboarding. */
    fun checkAppData() {
        Log.i("scp", "start checkAppData()")
        val updates = ScpApplication.dataUpdates
        updates.check()
        if (PreferenceUtil.isFirstInstallApp()) {
            PreferenceUtil.setFirstInstallApp()
            NewbieManager.showLevelDialog(activity)
            return
        }
        if (PreferenceUtil.getFirstOpenCurrentVersion(currentVersionCode.toString())) {
            PreferenceUtil.setFirstOpenCurrentVersion(currentVersionCode.toString())
            AppInfoDatabase.getInstance()
        }

        val host = activity
        host.lifecycleScope.launch {
            host.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                updates.state.collect { state ->
                    if (!state.busy && updates.claimMeteredPrompt()) confirmCatalogDownload(host)
                }
            }
        }

        if (Utils.enabledNetwork(activity)) {
            PreferenceUtil.addPoints(1)
            Log.i("scp", "checkUpdate")
            getConfig()
        }
    }

    private fun getConfig() {
        // Write a message to the database
        database.child("config").get().addOnSuccessListener {
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
                Log.i("scp", "current = $currentVersionCode, new = $newVersionCode, 需要升级")
                ScpDialogs.builder(activity)
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
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }
}
