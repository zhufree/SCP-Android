package info.free.scp

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.lzy.okgo.OkGo
import com.lzy.okserver.OkDownload
import com.tendcloud.tenddata.TCAgent
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
        OkDownload.getInstance().folder = filesDir.absolutePath

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
        UMConfigure.init(this, PrivateConstants.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, "")
//        MobclickAgent.openActivityDurationTrack(false)
        UMConfigure.setLogEnabled(true)
//        MobclickAgent.setCatchUncaughtExceptions(false)
        TCAgent.LOG_ON=true
        // App ID: 在TalkingData创建应用后，进入数据报表页中，在“系统设置”-“编辑应用”页面里查看App ID。
        // 渠道 ID: 是渠道标识符，可通过不同渠道单独追踪数据。
        TCAgent.init(this, PrivateConstants.TD_APP_KEY, BuildConfig.FLAVOR)
        // 如果已经在AndroidManifest.xml配置了App ID和渠道ID，调用TCAgent.init(this)即可；或与AndroidManifest.xml中的对应参数保持一致。
        TCAgent.setReportUncaughtExceptions(true)

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