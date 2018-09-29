package info.free.scp

import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDexApplication
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import info.free.scp.service.InitDataService
import info.free.scp.util.PreferenceUtil

/**
 * Created by zhufree on 2018/8/27.
 *
 */

class ScpApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
        UMConfigure.init(this, PrivateConstants.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, "")
//        MobclickAgent.openActivityDurationTrack(false)
        UMConfigure.setLogEnabled(true)


        context = applicationContext
    }
    companion object {
        var context: Context? = null
    }
}