package info.free.scp

import android.content.*
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import info.free.scp.SCPConstants.BroadCastAction.ACTION_CHANGE_THEME
import info.free.scp.SCPConstants.BroadCastAction.INIT_PROGRESS
import info.free.scp.util.*
import info.free.scp.view.user.UserFragment
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    private var currentFragment: BaseFragment? = null
    private val homeFragment = HomeFragment.newInstance()
    //    private val feedFragment = FeedFragment.newInstance()
    private val userFragment = UserFragment.newInstance()

    private var mLocalBroadcastManager: LocalBroadcastManager? = null
    private var fragmentInit = false


    private var themeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshTheme()
        }
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(currentFragment as androidx.fragment.app.Fragment)
        when (item.itemId) {
            R.id.navigation_home -> {
                if (!homeFragment.isAdded && null == supportFragmentManager.findFragmentByTag("home")) {
                    transaction.add(R.id.flMainContainer, homeFragment, "home")
                } else {
                    transaction.show(homeFragment)
                    transaction.hide(userFragment)
                }
                currentFragment = homeFragment
            }
            //            R.id.navigation_feed -> {
            //                if (feedFragment.isAdded) {
            //                    transaction.show(feedFragment)
            //                } else {
            //                    transaction.add(R.id.flMainContainer, feedFragment)
            //                }
            //                currentFragment = feedFragment
            //                return@OnNavigationItemSelectedListener true
            //            }
            R.id.navigation_about -> {
                if (!userFragment.isAdded && null == supportFragmentManager.findFragmentByTag("user")) {
                    transaction.add(R.id.flMainContainer, userFragment, "user")
                } else {
                    transaction.show(userFragment)
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
        // 恢复意外退出的数据（不知道有没有用）
        savedInstanceState?.let {
            currentFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment") as BaseFragment
        }

        // 设置默认fragment
        navigation?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        Logger.local = true
    }

    override fun onResume() {
        super.onResume()

        if (!fragmentInit) {
            // 设置默认fragment
            val transaction = supportFragmentManager.beginTransaction()
            if (currentFragment != null) {
                if (currentFragment?.isAdded == true) {
                    transaction.show(currentFragment!!)
                } else {
                    transaction.add(R.id.flMainContainer, currentFragment!!)
                }
            } else {
                transaction.add(R.id.flMainContainer, homeFragment)
                currentFragment = homeFragment
            }
            transaction.commit()
            UpdateManager.getInstance(this).checkAppData()
            fragmentInit = true
        }
    }

    /**
     * 注册广播
     * 1. 初始化目录
     * 2. 加载正文
     * 3. 主题
     */
    private fun registerBroadCastReceivers() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(INIT_PROGRESS)
        mLocalBroadcastManager?.registerReceiver(themeReceiver, IntentFilter(ACTION_CHANGE_THEME))
    }


    override fun onSaveInstanceState(outState: Bundle) {
        currentFragment?.let {
            supportFragmentManager.putFragment(outState, "currentFragment", it)
        }
        super.onSaveInstanceState(outState)
    }

    fun refreshTheme() {
        navigation.setBackgroundColor(ThemeUtil.containerBg)
        homeFragment.refreshTheme()
        userFragment.refreshTheme()
    }
}
