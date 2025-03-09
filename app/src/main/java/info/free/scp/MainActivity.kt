package info.free.scp

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import info.free.scp.databinding.ActivityMainBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.FileUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.UpdateManager
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.HomeFragment
import info.free.scp.view.later.LaterFragment
import info.free.scp.view.user.UserFragment
import org.jetbrains.anko.info
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private var currentFragment: BaseFragment? = null
    private val homeFragment = HomeFragment.newInstance()
    private val laterFragment = LaterFragment.newInstance()
    private val userFragment = UserFragment.newInstance()
    private lateinit var binding: ActivityMainBinding


    @AfterPermissionGranted(SCPConstants.RequestCode.REQUEST_FILE_PERMISSION)
    private fun requireFilePermission() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                if (!homeFragment.isAdded && null == supportFragmentManager
                                .findFragmentByTag(homeFragment.javaClass.name)) {
                    transaction.add(R.id.flMainContainer, homeFragment, homeFragment.javaClass.name)
                } else {
                    transaction.show(homeFragment)
                    transaction.hide(laterFragment)
                    transaction.hide(userFragment)
                }
                currentFragment = homeFragment
            }
            R.id.navigation_later -> {
                if (!laterFragment.isAdded && null == supportFragmentManager
                                .findFragmentByTag(laterFragment.javaClass.name)) {
                    transaction.add(R.id.flMainContainer, laterFragment, laterFragment.javaClass.name)
                } else {
                    transaction.show(laterFragment)
                    transaction.hide(homeFragment)
                    transaction.hide(userFragment)
                }
                currentFragment = laterFragment
            }
            R.id.navigation_about -> {
                if (!userFragment.isAdded && null == supportFragmentManager
                                .findFragmentByTag(userFragment.javaClass.name)) {
                    transaction.add(R.id.flMainContainer, userFragment, userFragment.javaClass.name)
                } else {
                    transaction.show(userFragment)
                    transaction.hide(laterFragment)
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val transaction = supportFragmentManager.beginTransaction()
        currentFragment?.let {
            if (it.isAdded) {
                transaction.show(it)
            } else {
                transaction.add(R.id.flMainContainer, it, it.javaClass.name)
            }
        } ?: run {
            transaction.add(R.id.flMainContainer, homeFragment, homeFragment.javaClass.name)
            currentFragment = homeFragment
        }
        transaction.commit()
        binding.navigation.setBackgroundColor(ThemeUtil.containerBg)
        UpdateManager.getInstance(this).checkAppData()
//        FileUtil.copyCategoryDb(false)
        FileUtil.checkDetailDb()

        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        requireFilePermission()

        ScpDatabase.getNewInstance()
        AppInfoDatabase.getNewInstance()

    }

    override fun onResume() {
        super.onResume()
        AppInfoDatabase.getInstance()
    }

    override fun refreshTheme() {
        super.refreshTheme()
        binding.navigation.setBackgroundColor(ThemeUtil.containerBg)
        homeFragment.refreshTheme()
        laterFragment.refreshTheme()
        userFragment.refreshTheme()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        info { "onPermissionsDenied" }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        info { "onPermissionsGranted" }
    }
}
