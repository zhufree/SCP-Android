package info.free.scp.view.base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.soulgame.sgsdk.tgsdklib.TGSDK
import com.umeng.analytics.MobclickAgent
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.util.ThemeUtil
import org.jetbrains.anko.AnkoLogger


open class BaseActivity : AppCompatActivity(), AnkoLogger {

    var baseToolbar: Toolbar? = null
        set(value) {
            field = value
            setSupportActionBar(value)
            value?.setNavigationOnClickListener { finish() }
            value?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        }

    private var themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshTheme()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
        registerBroadCastReceivers()
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

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    fun registerBroadCastReceivers() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        mLocalBroadcastManager?.registerReceiver(themeReceiver, IntentFilter(SCPConstants.BroadCastAction.ACTION_CHANGE_THEME))
    }

    open fun refreshTheme(){
        baseToolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
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
