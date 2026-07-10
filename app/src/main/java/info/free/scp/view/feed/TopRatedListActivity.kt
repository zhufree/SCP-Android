package info.free.scp.view.feed

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import info.free.scp.R
import info.free.scp.SCPConstants.TOP_RATED_ALL
import info.free.scp.SCPConstants.TOP_RATED_GOI
import info.free.scp.SCPConstants.TOP_RATED_SCP
import info.free.scp.SCPConstants.TOP_RATED_TALES
import info.free.scp.SCPConstants.TOP_RATED_WANDERS
import info.free.scp.ui.MainTheme
import info.free.scp.ui.feed.TopRatedFeedScreen
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity

class TopRatedListActivity : BaseActivity() {
    private val viewModel by viewModels<FeedListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val feedType = intent?.getIntExtra("feedType", 0) ?: 0
        val titleResId = when (feedType) {
            TOP_RATED_ALL -> R.string.top_rated_all
            TOP_RATED_SCP -> R.string.top_rated_scp
            TOP_RATED_TALES -> R.string.top_rated_tale
            TOP_RATED_GOI -> R.string.top_rated_goi
            TOP_RATED_WANDERS -> R.string.top_rated_wanderer
            else -> R.string.top_rated_all
        }
        setContent {
            val feedList by viewModel.getFeed().observeAsState(emptyList())
            LaunchedEffect(feedType) {
                viewModel.loadFeed(feedType)
            }
            MainTheme {
                TopRatedFeedScreen(
                    title = getString(titleResId),
                    feedList = feedList,
                    onBackPressed = { finish() },
                    onOpenFeed = { feed ->
                        startActivity(
                            Intent(this, DetailActivity::class.java)
                                .putExtra("link", feed.link)
                                .putExtra("forceOnline", true)
                        )
                    }
                )
            }
        }
    }
}
