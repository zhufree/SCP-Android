package info.free.scp.view.user

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.EventUtil
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.search.SearchResultAdapter
import kotlinx.android.synthetic.main.activity_like.*

class LikeActivity : BaseActivity() {
    val likeList = emptyList<ScpModel?>().toMutableList()
    var adapter : SearchResultAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        initToolbar()

        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_like?.layoutManager = lm
        likeList.addAll(ScpDao.getInstance().getLikeScpList())
        Log.i("search", likeList.size.toString())
        adapter = SearchResultAdapter(this, likeList)
        rv_like?.adapter = adapter
        adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent()
                intent.putExtra("link", likeList[position]?.link)
                intent.putExtra("sId", likeList[position]?.sId)
                intent.setClass(this@LikeActivity, DetailActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun initToolbar() {
        setSupportActionBar(like_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        like_toolbar?.title = "收藏"
        like_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        like_toolbar?.setNavigationOnClickListener { finish() }
    }
}
