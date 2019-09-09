package info.free.scp.view.like

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import info.free.scp.R
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.EventUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_like.*

class LikeBoxActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_box)
        EventUtil.onEvent(this, EventUtil.clickLikeList)
        initToolbar()

        val lm = LinearLayoutManager(this, VERTICAL, false)
        rv_like?.layoutManager = lm
        val adapter = LikeBoxAdapter()
        rv_like?.adapter = adapter
        adapter.submitList(AppInfoDatabase.getInstance().likeAndReadDao().getLikeBox())
    }

    private fun initToolbar() {
        baseToolbar = like_toolbar
        supportActionBar?.title = "收藏列表"
    }
}
