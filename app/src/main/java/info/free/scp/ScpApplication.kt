package info.free.scp

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import com.umeng.analytics.AnalyticsConfig
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity

/**
 * Created by zhufree on 2018/8/27.
 * 自定义application
 */

class ScpApplication : MultiDexApplication() {



    override fun onCreate() {
        super.onCreate()

        UMConfigure.init(this, PrivateConstants.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, "")
        UMConfigure.setLogEnabled(true)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
        // FIXME
        MobclickAgent.setCatchUncaughtExceptions(!isDebug)


        context = applicationContext

        ThemeUtil.setTheme(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
                if (activity is BaseActivity) {
                    currentActivity = activity
                }
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
        return resources
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
        lateinit var context: Context
        var isDebug = true
        var currentActivity: BaseActivity? = null
        val downloadManager by lazy {
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }
        val channelName:String
            get() {
                return AnalyticsConfig.getChannel(context)
            }
    }
}
