package info.free.scp.view.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.soulgame.sgsdk.tgsdklib.TGSDK
import com.umeng.analytics.MobclickAgent
import info.free.scp.util.ThemeUtil
import org.jetbrains.anko.AnkoLogger


open class BaseActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
    }

    public override fun onResume() {
        MobclickAgent.onResume(this)
        TGSDK.onResume(this)

        super.onResume()
    }

    public override fun onPause() {
        MobclickAgent.onPause(this)
        TGSDK.onPause(this)

        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        TGSDK.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        TGSDK.onStop(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        TGSDK.onDestroy(this)
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)
        TGSDK.onActivityResult(this, reqCode, resCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        TGSDK.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
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
}
