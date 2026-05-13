package info.free.scp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.ui.MainTheme
import info.free.scp.util.FileUtil
import info.free.scp.util.UpdateManager
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.HomeFragment
import info.free.scp.view.later.LaterFragment
import info.free.scp.view.user.UserFragment

class MainActivity : BaseActivity() {
    private companion object {
        const val KEY_SELECTED_TAB = "main.selected.tab"
        private const val TAG_HOME = "main.fragment.home"
        private const val TAG_LATER = "main.fragment.later"
        private const val TAG_USER = "main.fragment.user"
    }

    private var currentFragment: BaseFragment? = null
    private lateinit var homeFragment: BaseFragment
    private lateinit var laterFragment: BaseFragment
    private lateinit var userFragment: BaseFragment
    private val fragmentContainerId = R.id.flMainContainer
    private var selectedTab by mutableStateOf(MainTab.HOME)
    private var themeVersion by mutableIntStateOf(0)

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        Log.i("scp", "storage permission granted=$allGranted")
    }

    private fun requireFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return
        }
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val hasAllPermissions = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!hasAllPermissions) {
            storagePermissionLauncher.launch(permissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFragments()
        selectedTab = savedInstanceState?.getString(KEY_SELECTED_TAB)
            ?.let(MainTab::fromName)
            ?: MainTab.HOME
        setContent {
            themeVersion
            MainTheme {
                MainShell(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                    },
                    onTabCommitted = { tab ->
                        switchFragment(tab)
                    },
                    fragmentContainerId = fragmentContainerId
                )
            }
        }
        UpdateManager.getInstance(this).checkAppData()
        FileUtil.checkDetailDb()
        requireFilePermission()
        ScpDatabase.getNewInstance()
        AppInfoDatabase.getNewInstance()
    }

    override fun onResume() {
        super.onResume()
        AppInfoDatabase.getInstance()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SELECTED_TAB, selectedTab.name)
    }

    private fun initFragments() {
        homeFragment = (supportFragmentManager.findFragmentByTag(TAG_HOME) as? BaseFragment)
            ?: HomeFragment.newInstance()
        laterFragment = (supportFragmentManager.findFragmentByTag(TAG_LATER) as? BaseFragment)
            ?: LaterFragment.newInstance()
        userFragment = (supportFragmentManager.findFragmentByTag(TAG_USER) as? BaseFragment)
            ?: UserFragment.newInstance()

        currentFragment = when {
            homeFragment.isAdded && !homeFragment.isHidden -> homeFragment
            laterFragment.isAdded && !laterFragment.isHidden -> laterFragment
            userFragment.isAdded && !userFragment.isHidden -> userFragment
            else -> null
        }
    }

    private fun switchFragment(tab: MainTab) {
        val targetFragment = when (tab) {
            MainTab.HOME -> homeFragment
            MainTab.LATER -> laterFragment
            MainTab.USER -> userFragment
        }
        if (currentFragment === targetFragment && targetFragment.isAdded && !targetFragment.isHidden) {
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        if (!targetFragment.isAdded) {
            transaction.add(
                fragmentContainerId,
                targetFragment,
                when (tab) {
                    MainTab.HOME -> TAG_HOME
                    MainTab.LATER -> TAG_LATER
                    MainTab.USER -> TAG_USER
                }
            )
        }
        currentFragment?.let { current ->
            if (current != targetFragment && current.isAdded) {
                transaction.hide(current as Fragment)
            }
        }
        transaction.show(targetFragment)
        currentFragment = targetFragment
        transaction.commitAllowingStateLoss()
    }

    override fun refreshTheme() {
        super.refreshTheme()
        themeVersion++
        homeFragment.refreshTheme()
        laterFragment.refreshTheme()
        userFragment.refreshTheme()
    }
}

private enum class MainTab(
    val iconRes: Int,
    val titleRes: Int
) {
    HOME(R.drawable.ic_home_black_24dp, R.string.tab_home),
    LATER(R.drawable.baseline_menu_book_black_24dp, R.string.later),
    USER(R.drawable.baseline_face_black_24dp, R.string.tab_mine);

    companion object {
        fun fromName(value: String): MainTab? = entries.firstOrNull { it.name == value }
    }
}

@Composable
private fun MainShell(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onTabCommitted: (MainTab) -> Unit,
    fragmentContainerId: Int
) {
    val context = LocalContext.current
    LaunchedEffect(selectedTab) {
        onTabCommitted(selectedTab)
    }
    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.onSurface,
                contentColor = MaterialTheme.colors.onPrimary
            ) {
                MainTab.entries.forEach { tab ->
                    BottomNavigationItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = stringResource(tab.titleRes)
                            )
                        },
                        label = { Text(stringResource(tab.titleRes)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = {
                FrameLayout(context).apply {
                    id = fragmentContainerId
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    }
}
