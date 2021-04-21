package info.free.scp.view.feed

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_top_rated.*
import org.jetbrains.anko.startActivity

class TopRatedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_rated)
        baseToolbar = top_rated_toolbar
        cv_top_rated_all.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_ALL)
        }
        cv_top_rated_scp?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_SCP)
        }
        cv_top_rated_tales?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_TALES)
        }
        cv_top_rated_goi?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_GOI)
        }
        cv_top_rated_wanderer?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_WANDERS)
        }
    }
}