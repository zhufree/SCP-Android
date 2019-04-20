package info.free.scp.view.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.search.SearchResultAdapter
import kotlinx.android.synthetic.main.activity_like.*

class LikeActivity : BaseActivity() {
    val likeList = emptyList<ScpModel?>().toMutableList()
    var adapter : SearchResultAdapter? = null
    private var orderType = 0 // 0 默认顺序 1 按编号
        set(value) {
            field = value
            likeList.clear()
            likeList.addAll(if (value == 0) ScpDataHelper.getInstance().getLikeScpList() else
                ScpDataHelper.getInstance().getOrderedLikeList())
            adapter?.notifyDataSetChanged()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        EventUtil.onEvent(this, EventUtil.clickLikeList)
        initToolbar()

        val lm = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        rv_like?.layoutManager = lm
        likeList.addAll(ScpDataHelper.getInstance().getLikeScpList())
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
        supportActionBar?.title = "收藏列表"
        like_toolbar?.inflateMenu(R.menu.category_menu)
        like_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        like_toolbar?.setNavigationOnClickListener { finish() }
        like_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    orderType = if (orderType == 0) 1 else 0
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }
}
