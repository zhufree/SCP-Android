package info.free.scp

import android.Manifest
import android.content.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import info.free.scp.SCPConstants.BroadCastAction.ACTION_CHANGE_THEME
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.*
import info.free.scp.view.user.UserFragment
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.feed.FeedFragment
import info.free.scp.view.home.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private var currentFragment: BaseFragment? = null
    private val homeFragment = HomeFragment.newInstance()
    private val feedFragment = FeedFragment.newInstance()
    private val userFragment = UserFragment.newInstance()

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    private var themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshTheme()
        }
    }

    @AfterPermissionGranted(SCPConstants.RequestCode.REQUEST_FILE_PERMISSION)
    private fun requireFilePermission() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.request_permission_notice),
                    SCPConstants.RequestCode.REQUEST_FILE_PERMISSION, *perms)
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(currentFragment as Fragment)
        when (item.itemId) {
            R.id.navigation_home -> {
                if (!homeFragment.isAdded && null == supportFragmentManager.findFragmentByTag("home")) {
                    transaction.add(R.id.flMainContainer, homeFragment, "home")
                } else {
                    transaction.show(homeFragment)
                    transaction.hide(feedFragment)
                    transaction.hide(userFragment)
                }
                currentFragment = homeFragment
            }
            R.id.navigation_feed -> {
                if (!feedFragment.isAdded && null == supportFragmentManager.findFragmentByTag("feed")) {
                    transaction.add(R.id.flMainContainer, feedFragment, "feed")
                } else {
                    transaction.show(feedFragment)
                    transaction.hide(homeFragment)
                    transaction.hide(userFragment)
                }
                currentFragment = feedFragment
            }
            R.id.navigation_about -> {
                if (!userFragment.isAdded && null == supportFragmentManager.findFragmentByTag("user")) {
                    transaction.add(R.id.flMainContainer, userFragment, "user")
                } else {
                    transaction.show(userFragment)
                    transaction.hide(feedFragment)
                    transaction.hide(homeFragment)
                }
                currentFragment = userFragment
            }
        }
        transaction.commitAllowingStateLoss()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注册广播
        registerBroadCastReceivers()

        setContentView(R.layout.activity_main)

        // 设置默认fragment
        val transaction = supportFragmentManager.beginTransaction()
        currentFragment?.let {
            if (it.isAdded) {
                transaction.show(it)
            } else {
                transaction.add(R.id.flMainContainer, it)
            }
        } ?: run {
            transaction.add(R.id.flMainContainer, homeFragment, "home")
            currentFragment = homeFragment
        }
        transaction.commit()
        UpdateManager.getInstance(this).checkAppData()

        navigation?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        requireFilePermission()
    }

    override fun onResume() {
        super.onResume()
        AppInfoDatabase.getInstance()
    }

    /**
     * 注册广播：主题
     */
    private fun registerBroadCastReceivers() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        mLocalBroadcastManager?.registerReceiver(themeReceiver, IntentFilter(ACTION_CHANGE_THEME))
    }

    fun refreshTheme() {
        navigation.setBackgroundColor(ThemeUtil.containerBg)
        homeFragment.refreshTheme()
        userFragment.refreshTheme()
        feedFragment.refreshTheme()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }
}
