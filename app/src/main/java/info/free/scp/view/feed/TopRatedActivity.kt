package info.free.scp.view.feed

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import info.free.scp.ui.MainTheme
import info.free.scp.ui.feed.TopRatedMenuScreen
import info.free.scp.view.base.BaseActivity

class TopRatedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                TopRatedMenuScreen(
                    showTopBar = true,
                    onBackPressed = { finish() },
                    onOpenFeedType = ::openTopRatedList
                )
            }
        }
    }

    private fun openTopRatedList(feedType: Int) {
        startActivity(
            Intent(this, TopRatedListActivity::class.java)
                .putExtra("feedType", feedType)
        )
    }
}
