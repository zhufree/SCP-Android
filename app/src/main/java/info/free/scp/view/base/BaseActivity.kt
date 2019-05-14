package info.free.scp.view.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tendcloud.tenddata.TCAgent
import com.umeng.analytics.MobclickAgent
import info.free.scp.util.ThemeUtil
import org.jetbrains.anko.AnkoLogger


open class BaseActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
    }

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
        TCAgent.onPageStart(this, localClassName)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
        TCAgent.onPageEnd(this, localClassName)
    }

    /**
     * wifi开启检查
     *
     * @return
     */
    fun Activity.enabledWifi(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifiManager != null && wifiManager.wifiState == WifiManager.WIFI_STATE_ENABLED
    }

    /**
     * 3G网开启检查
     *
     * @return
     */
    fun Activity.enabledNetwork(): Boolean {
        val cManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cManager?.activeNetworkInfo != null && cManager.activeNetworkInfo.isAvailable
    }

    fun Activity.toActivity(activity: Class<*>, intent: Intent) {
        intent.setClass(this, activity)
        startActivity(intent)
    }

}
