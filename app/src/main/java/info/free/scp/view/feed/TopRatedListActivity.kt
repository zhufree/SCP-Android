package info.free.scp.view.feed

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants.TOP_RATED_ALL
import info.free.scp.SCPConstants.TOP_RATED_GOI
import info.free.scp.SCPConstants.TOP_RATED_SCP
import info.free.scp.SCPConstants.TOP_RATED_TALES
import info.free.scp.SCPConstants.TOP_RATED_WANDERS
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_top_rated_list.*

class TopRatedListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_rated_list)
        baseToolbar = top_rated_toolbar
        val feedType = intent?.getIntExtra("feedType", 0) ?: 0
        val titleResId = when (feedType) {
            TOP_RATED_ALL -> R.string.top_rated_all
            TOP_RATED_SCP -> R.string.top_rated_scp
            TOP_RATED_TALES -> R.string.top_rated_tale
            TOP_RATED_GOI -> R.string.top_rated_goi
            TOP_RATED_WANDERS -> R.string.top_rated_wanderer
            else -> R.string.top_rated_all
        }
        supportActionBar?.setTitle(titleResId)
        supportFragmentManager.beginTransaction().replace(R.id.fl_top_rated,
                SubFeedFragment.newInstance(feedType)).commitAllowingStateLoss()
    }
}
