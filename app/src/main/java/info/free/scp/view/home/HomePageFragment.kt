package info.free.scp.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.data.settings.HomePreferencesStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.ui.MainTheme
import info.free.scp.ui.home.HomePageScreen
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.GroupListActivity
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.feed.FeedActivity
import info.free.scp.view.feed.FeedListViewModel
import info.free.scp.view.feed.TopRatedActivity
import info.free.scp.view.tag.TagCloudActivity

class HomePageFragment : BaseFragment() {
    private val homePreferences: HomePreferencesStore = PreferenceSettingsStore()
    // 挂在父 Fragment 上：首页 Tab 被替换掉再切回来时不丢状态，也不会每次重新请求
    private val feedVm by lazy {
        ViewModelProvider(requireParentFragment())[FeedListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val feedState by feedVm.feedIndexState.collectAsState()
                var showNotice by remember { mutableStateOf(homePreferences.shouldShowNotice) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val dismissedMessage = stringResource(R.string.home_notice_dismissed)
                MainTheme {
                    HomePageScreen(
                        notice = homePreferences.notice,
                        showNotice = showNotice,
                        createState = feedState.create,
                        translateState = feedState.translate,
                        snackbarHostState = snackbarHostState,
                        onDismissNotice = {
                            homePreferences.dismissNotice()
                            showNotice = false
                            scope.launch { snackbarHostState.showSnackbar(dismissedMessage) }
                        },
                        onOpenGroup = { goToGroupPage(it) },
                        onOpenFeed = { startActivity(Intent(requireContext(), FeedActivity::class.java)) },
                        onRetryFeed = { feedVm.loadFeedIndex(force = true) },
                        onOpenTopRated = { startActivity(Intent(requireContext(), TopRatedActivity::class.java)) },
                        onOpenTags = { startActivity(Intent(requireContext(), TagCloudActivity::class.java)) },
                        onOpenFeedItem = { feed ->
                            startActivity(
                                Intent(requireContext(), DetailActivity::class.java)
                                    .putExtra("link", feed.link)
                                    .putExtra("forceOnline", true)
                            )
                        }
                    )
                }
                LaunchedEffect(Unit) {
                    feedVm.loadFeedIndex()
                }
            }
        }
    }

    private fun goToGroupPage(entryType: Int) {
        startActivity(
            Intent(requireContext(), GroupListActivity::class.java)
                .putExtra("entry_type", entryType)
        )
    }

    companion object {
        fun newInstance(): HomePageFragment = HomePageFragment()
    }
}
