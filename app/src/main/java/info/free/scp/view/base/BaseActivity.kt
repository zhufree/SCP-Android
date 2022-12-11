package info.free.scp.view.base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
            value?.setNavigationIcon(R.drawable.baseline_arrow_back_white_24dp)
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

    fun requestReadFileTree() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*";
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        startActivityForResult(intent, SCPConstants.RequestCode.REQUEST_PUBLIC_FILE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    fun registerBroadCastReceivers() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        mLocalBroadcastManager?.registerReceiver(themeReceiver, IntentFilter(SCPConstants.BroadCastAction.ACTION_CHANGE_THEME))
    }

    open fun refreshTheme() {
        baseToolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        window.statusBarColor = ThemeUtil.toolbarBg
        window.navigationBarColor = ThemeUtil.containerBg
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
