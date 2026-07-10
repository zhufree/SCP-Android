package info.free.scp.view.feed

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.SCPConstants.LATEST_TRANSLATED
import info.free.scp.ui.MainTheme
import info.free.scp.ui.feed.LatestFeedScreen
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity

class FeedActivity : BaseActivity() {
    private val createdViewModel by lazy {
        ViewModelProvider(this)["latest_created", FeedListViewModel::class.java]
    }
    private val translatedViewModel by lazy {
        ViewModelProvider(this)["latest_translated", FeedListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val createdFeedList by createdViewModel.getFeed().observeAsState(emptyList())
            val translatedFeedList by translatedViewModel.getFeed().observeAsState(emptyList())
            LaunchedEffect(Unit) {
                createdViewModel.loadFeed(LATEST_CREATED)
                translatedViewModel.loadFeed(LATEST_TRANSLATED)
            }
            MainTheme {
                LatestFeedScreen(
                    createdFeedList = createdFeedList,
                    translatedFeedList = translatedFeedList,
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
