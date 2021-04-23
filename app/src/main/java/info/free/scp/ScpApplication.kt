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
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity

/**
 * Created by zhufree on 2018/8/27.
 * 自定义application
 */

class ScpApplication : MultiDexApplication() {



    override fun onCreate() {
        super.onCreate()
        // remove this if don't need analyze user data
        UMConfigure.init(this, PrivateConstants.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, "")
        UMConfigure.setLogEnabled(true)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
        // FIXME
        MobclickAgent.setCatchUncaughtExceptions(!isDebug)
        // ============================================

        // remove this if don't need ad
//        LogManager.init(LogConfig.Builder)
        // 设置 false 可以关闭 SDK 日志
//        LEConfig.setEnableLogger(false);
        // 参数1为channel：标识渠道（可随意填写），不要出现特殊字符即可；参数2为APPID(在开发者后台获取，也可联系客服获取)
//        LESdk.initSDK("kuan", "10225");
        // 图片加载器设置，实现 ImageLoader，详情参考 Demo
//        LESdk.setImageLoader(GlideImageLoader());
        // ============================================

        context = applicationContext

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

    override fun getResources(): Resources {
        val resources = super.getResources()
        if (resources != null && resources.configuration.fontScale != 1.0f) {
            resources.configuration.fontScale = 1.0f
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)
        }
        return resources
    }

    override fun onTerminate() {
        AppInfoDatabase.getInstance().close()
        super.onTerminate()
    }

    companion object {
        lateinit var context: Context
        var isDebug = false
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
