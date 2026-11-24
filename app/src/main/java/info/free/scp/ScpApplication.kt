package info.free.scp

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import info.free.scp.PrivateConstants.FIREBASE_URL
import info.free.scp.data.update.DataUpdates
import info.free.scp.data.update.UpdateDatabases
import info.free.scp.db.AppInfoDatabase
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
        UpdateDatabases(context).installAtColdStart()

        database = Firebase.database(FIREBASE_URL).reference

        ThemeUtil.setTheme(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
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

    override fun onTerminate() {
        AppInfoDatabase.getInstance().close()
        super.onTerminate()
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
