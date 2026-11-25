package info.free.scp

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import info.free.scp.PrivateConstants.FIREBASE_URL
import info.free.scp.data.backup.AndroidAppDataBackupRepository
import info.free.scp.data.update.DataUpdates
import info.free.scp.data.update.UpdateDatabases
import info.free.scp.util.ScpDialogs
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity

/**
 * Created by zhufree on 2018/8/27.
 * 自定义application
 */

class ScpApplication : Application() {

    override fun attachBaseContext(base: Context) {
        val config = Configuration(base.resources.configuration)
        config.fontScale = 1.0f
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
        val startupMessages = mutableListOf<String>()
        try {
            AndroidAppDataBackupRepository(context).restoreAtColdStart(startupMessages::add)
        } catch (failure: Exception) {
            Log.w("ScpBackup", "Cold-start restore recovery failed", failure)
            startupMessages.add("用户数据恢复未完成，请重启应用以重试回滚")
        }
        UpdateDatabases(context).installAtColdStart(startupMessages::add)

        database = Firebase.database(FIREBASE_URL).reference

        ThemeUtil.setTheme(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                if (startupMessages.isEmpty() || activity.isFinishing) return
                ScpDialogs.showMessage(activity, startupMessages.joinToString("\n"))
                startupMessages.clear()
            }

            override fun onActivityStarted(activity: Activity) {
                if (activity is BaseActivity) {
                    currentActivity = activity
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }
        })
    }

    companion object {
        lateinit var context: Context
        internal val dataUpdates by lazy { DataUpdates(context) }
        lateinit var database: DatabaseReference
        var isDebug = true
        var currentActivity: BaseActivity? = null
        val downloadManager by lazy {
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }
    }
}
