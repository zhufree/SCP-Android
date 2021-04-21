package info.free.scp.view.feed

import android.os.Bundle
import info.free.scp.R
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        baseToolbar = feed_toolbar
        supportActionBar?.title = "最近更新"

        supportFragmentManager.beginTransaction()
                .add(R.id.fl_feed, FeedFragment.newInstance(), "f_feed").commit()
    }
}