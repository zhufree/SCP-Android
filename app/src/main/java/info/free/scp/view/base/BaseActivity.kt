package info.free.scp.view.base

import android.app.Activity
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import info.free.scp.R
import info.free.scp.util.ThemeUtil


open class BaseActivity : AppCompatActivity() {

    var baseToolbar: Toolbar? = null
        set(value) {
            field = value
            setSupportActionBar(value)
            value?.setNavigationOnClickListener { finish() }
            value?.setNavigationIcon(R.drawable.baseline_arrow_back_white_24dp)
        }

    private val themeListener: () -> Unit = { refreshTheme() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
        ThemeUtil.registerThemeListener(themeListener)
        window.statusBarColor = ThemeUtil.toolbarBg
    }


    public override fun onResume() {
//        MobclickAgent.onResume(this) // remove this if don't need analyze user data
        super.onResume()
    }

    public override fun onPause() {
//        MobclickAgent.onPause(this) // remove this if don't need analyze user data
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    open fun refreshTheme() {
        baseToolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        window.statusBarColor = ThemeUtil.toolbarBg
        window.navigationBarColor = ThemeUtil.containerBg
    }

    override fun onDestroy() {
        ThemeUtil.unregisterThemeListener(themeListener)
        super.onDestroy()
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
//    fun Activity.enabledNetwork(): Boolean {
//        val cManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//        return cManager?.activeNetworkInfo != null && cManager.activeNetworkInfo.isAvailable
//    }
}
