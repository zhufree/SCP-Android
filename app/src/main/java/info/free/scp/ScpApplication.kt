package info.free.scp

import android.content.Context
import android.support.multidex.MultiDexApplication
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
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
        UMConfigure.init(this, PrivateConstants.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, "")
//        MobclickAgent.openActivityDurationTrack(false)
        UMConfigure.setLogEnabled(true)
//        MobclickAgent.setCatchUncaughtExceptions(false)
        context = applicationContext

        ThemeUtil.setTheme(this)

    }

    override fun onTerminate() {
        Logger.saveLog()
        super.onTerminate()
    }

    companion object {
        lateinit var context: Context
    }
}