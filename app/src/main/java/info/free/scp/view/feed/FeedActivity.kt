package info.free.scp.view.feed

import android.os.Bundle
import info.free.scp.R
import info.free.scp.databinding.ActivityFeedBinding
import info.free.scp.view.base.BaseActivity

class FeedActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.feedToolbar
        supportActionBar?.title = "最近更新"

        supportFragmentManager.beginTransaction()
            .add(R.id.fl_feed, FeedFragment.newInstance(), "f_feed").commit()
    }
}