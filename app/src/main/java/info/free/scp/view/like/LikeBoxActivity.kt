package info.free.scp.view.like

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import info.free.scp.R
import info.free.scp.bean.ScpLikeBox
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.EventUtil
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_like.*

class LikeBoxActivity : BaseActivity() {
    val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like_box)
        EventUtil.onEvent(this, EventUtil.clickLikeList)
        baseToolbar = like_toolbar
        supportActionBar?.title = "收藏列表"

        val lm = LinearLayoutManager(this, VERTICAL, false)
        rv_like?.layoutManager = lm
        val adapter = LikeBoxAdapter()
        rv_like?.adapter = adapter
        val boxList = arrayListOf<ScpLikeBox>()
        boxList.addAll(likeDao.getLikeBox())
        if (boxList.isEmpty()) {
            val defaultBox = ScpLikeBox(0, "默认收藏夹")
            boxList.add(defaultBox)
            likeDao.addLikeBox(defaultBox)
        }
        adapter.submitList(boxList)
    }
}
