package info.free.scp

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import com.lzy.okgo.OkGo
import com.lzy.okserver.OkDownload
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import info.free.scp.util.Logger
import info.free.scp.util.ThemeUtil

/**
 * Created by zhufree on 2018/8/27.
 * 自定义application
 */

class ScpApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        OkGo.getInstance().init(this)
        // /data/data/packageName/databases
//        val dbPath = ("$absPath${sp}data$sp$packageName${sp}databases$sp")
        OkDownload.getInstance().folder = cacheDir.absolutePath

        UMConfigure.init(this, PrivateConstants.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, "")
        UMConfigure.setLogEnabled(true)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
        // FIXME
        MobclickAgent.setCatchUncaughtExceptions(false)

        context = applicationContext

        ThemeUtil.setTheme(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
                currentActivity = activity
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }
        })
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        if (resources != null && resources.configuration.fontScale != 1.0f) {
            resources.configuration.fontScale = 1.0f
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)
        }
        return resources;
    }

    override fun onTerminate() {
        Logger.saveLog()
        super.onTerminate()
    }

    companion object {
        lateinit var context: Context
        var currentActivity: Activity? = null
    }
}