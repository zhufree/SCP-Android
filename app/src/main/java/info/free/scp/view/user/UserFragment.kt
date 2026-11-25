package info.free.scp.view.user

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import info.free.scp.BuildConfig
import info.free.scp.bean.ScpRecordModel
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.data.settings.UserPreferencesStore
import info.free.scp.ui.MainTheme
import info.free.scp.ui.user.UserDashboardScreen
import info.free.scp.ui.user.UserDashboardViewModel
import info.free.scp.util.EventUtil
import info.free.scp.util.NewbieManager
import info.free.scp.util.ScpDialogs
import info.free.scp.util.ThemeUtil
import info.free.scp.util.ThemeUtil.DAY_THEME
import info.free.scp.util.ThemeUtil.NIGHT_THEME
import info.free.scp.util.Utils
import info.free.scp.util.copyFileFromUri
import info.free.scp.util.privatePictureDir
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.draft.DraftListActivity

class UserFragment : BaseFragment() {
    private val userPreferences: UserPreferencesStore = PreferenceSettingsStore()
    private var userHeadBitmap by mutableStateOf<Bitmap?>(null)
    private var profileVersion by mutableStateOf(0)
    private val pickUserHeadImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val imgFile = privatePictureDir(requireContext(), "user_head.jpg")
                copyFileFromUri(requireContext(), it, imgFile)
                loadHeadImg()
            }
        }

    private val dashboardViewModel by lazy {
        ViewModelProvider(this)[UserDashboardViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by dashboardViewModel.uiState.collectAsState()
                val version = profileVersion
                MainTheme {
                    UserDashboardScreen(
                        nickname = userPreferences.nickname,
                        userCode = userPreferences.userCode,
                        jobTitle = jobTitle(version),
                        readCount = uiState.readCount,
                        likeCount = uiState.likeCount,
                        userHead = userHeadBitmap,
                        recentHistory = uiState.recentHistory,
                        showDonation = BuildConfig.FLAVOR != "GooglePlayProduction",
                        showOtherWork = userPreferences.showOtherWork,
                        onToggleTheme = {
                            ThemeUtil.changeTheme(
                                activity,
                                if (ThemeUtil.currentTheme == NIGHT_THEME) DAY_THEME else NIGHT_THEME
                            )
                        },
                        onPickUserHead = { pickUserHeadImage.launch("image/*") },
                        onEditNickname = { checkUserInfo() },
                        onEditJob = { checkJob() },
                        onOpenDraft = { startActivity(Intent(requireContext(), DraftListActivity::class.java)) },
                        onOpenGame = { startActivity(Intent(requireContext(), GameListActivity::class.java)) },
                        onOpenPortal = { startActivity(Intent(requireContext(), PortalListActivity::class.java)) },
                        onOpenReadSettings = {
                            EventUtil.onEvent(activity, EventUtil.clickReadSetting)
                            startActivity(Intent(requireContext(), SettingsActivity::class.java))
                        },
                        onOpenLocalData = { startActivity(Intent(requireContext(), DownloadActivity::class.java)) },
                        onOpenAbout = { startActivity(Intent(requireContext(), AboutAppActivity::class.java)) },
                        onOpenQuery = { Utils.openUrl(userPreferences.queryLink) },
                        onOpenOtherWork = { startActivity(Intent(requireContext(), OtherAppActivity::class.java)) },
                        onOpenDonation = { startActivity(Intent(requireContext(), DonationQrActivity::class.java)) },
                        onOpenMoreHistory = { startActivity(Intent(requireContext(), LaterAndHistoryActivity::class.java)) },
                        onOpenHistoryItem = { openHistoryItem(it) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!userPreferences.hasAcceptedPrivacy) {
            NewbieManager.showPrivacyDialog(requireActivity())
        }
        loadHeadImg()
        dashboardViewModel.refreshUserDashboard()
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.refreshUserDashboard()
    }

    private fun openHistoryItem(scp: ScpRecordModel) {
        startActivity(
            Intent(requireContext(), DetailActivity::class.java)
                .putExtra("link", scp.link)
                .putExtra("title", scp.title)
        )
    }

    /** 未设置代号时职务也视为未设置（与旧版“点击设置”的判断一致）；职业没选时同样显示“点击设置”。 */
    @Suppress("UNUSED_PARAMETER")
    private fun jobTitle(version: Int): String {
        if (userPreferences.nickname.isEmpty()) return ""
        return getRank(userPreferences.points)
    }

    private fun getRank(point: Int): String {
        return when {
            point < 200 -> userPreferences.getJobNameForLevel(0)
            point < 700 -> userPreferences.getJobNameForLevel(1)
            point < 1500 -> userPreferences.getJobNameForLevel(2)
            point < 2500 -> "C\u7ea7${userPreferences.getJobNameForLevel(3)}"
            point < 4000 -> "B\u7ea7${userPreferences.getJobNameForLevel(4)}"
            else -> "A\u7ea7${userPreferences.getJobNameForLevel(5)}"
        }
    }

    private fun loadHeadImg() {
        val imgFile = privatePictureDir(requireContext(), "user_head.jpg")
        userHeadBitmap = if (imgFile.exists()) {
            BitmapFactory.decodeFile(imgFile.path)
        } else {
            null
        }
    }

    private fun checkUserInfo() {
        ScpDialogs.showInput(
            context = requireContext(),
            title = "\u6b22\u8fce\u6765\u5230 SCP \u57fa\u91d1\u4f1a\uff0c\u8c03\u67e5\u5458\uff0c\u8bf7\u8f93\u5165\u4f60\u7684\u540d\u5b57",
            label = "\u540d\u5b57",
            initialText = userPreferences.nickname,
            positiveText = "\u786e\u5b9a",
            negativeText = "\u53d6\u6d88"
        ) { name ->
            userPreferences.saveNickname(name)
            profileVersion += 1
            checkJob()
        }
    }

    private fun checkJob() {
        val jobList = listOf(
            "\u6536\u5bb9\u4e13\u5bb6",
            "\u7814\u7a76\u5458",
            "\u5b89\u4fdd\u4eba\u5458",
            "\u6218\u672f\u53cd\u5e94\u4eba\u5458",
            "\u5916\u52e4\u7279\u5de5",
            "\u673a\u52a8\u7279\u9063\u961f\u6210\u5458"
        )
        ScpDialogs.showChoice(
            requireContext(),
            "\u6b22\u8fce\u6765\u5230 SCP \u57fa\u91d1\u4f1a\uff0c${userPreferences.nickname}\uff0c\u8bf7\u9009\u62e9\u4f60\u7684\u804c\u4e1a",
            jobList
        ) { i ->
            userPreferences.saveJob(jobList[i])
            profileVersion += 1
        }
    }

    companion object {
        fun newInstance(): UserFragment = UserFragment()
    }
}
