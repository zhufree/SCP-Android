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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
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
import toast

class HomePageFragment : BaseFragment() {
    private val homePreferences: HomePreferencesStore = PreferenceSettingsStore()
    private val feedVm by lazy {
        ViewModelProvider(this)[FeedListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val feedIndex by feedVm.getFeedIndex().observeAsState()
                var showNotice by remember { mutableStateOf(homePreferences.shouldShowNotice) }
                MainTheme {
                    HomePageScreen(
                        notice = homePreferences.notice,
                        showNotice = showNotice,
                        latestCreate = feedIndex?.latestCreate.orEmpty(),
                        latestTranslate = feedIndex?.latestTranslate.orEmpty(),
                        onDismissNotice = {
                            homePreferences.dismissNotice()
                            showNotice = false
                            toast("\u516c\u544a\u5df2\u9690\u85cf\uff0c\u53ef\u5728app\u4f7f\u7528\u8bf4\u660e\u4e2d\u67e5\u770b")
                        },
                        onOpenGroup = { goToGroupPage(it) },
                        onOpenFeed = { startActivity(Intent(requireContext(), FeedActivity::class.java)) },
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
