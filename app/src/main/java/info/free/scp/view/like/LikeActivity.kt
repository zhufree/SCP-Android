package info.free.scp.view.like

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import info.free.scp.R
import info.free.scp.bean.ScpLikeModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.EventUtil
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.user.SimpleScpAdapter
import kotlinx.android.synthetic.main.activity_like.*

class LikeActivity : BaseActivity() {
    val likeList = emptyList<ScpLikeModel?>().toMutableList()
    var adapter : SimpleScpAdapter? = null
    val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
//    private var orderType = 0 // 0 默认顺序 1 按编号
//        set(value) {
//            field = value
////            likeList.clear()
//            // TODO order改成拿到之后在总表里用index排序
////            likeList.addAll(if (value == 0) likeDao.getLikeList() else
////                likeDao.getOrderedLikeList())
////            if (value == 0)
////            likeList.sortBy { it?.title?:"" }
//            adapter?.notifyDataSetChanged()
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        EventUtil.onEvent(this, EventUtil.clickLikeList)
        initToolbar()

        val boxId = intent.getIntExtra("box_id", 0)
        val lm = LinearLayoutManager(this, VERTICAL, false)
        rv_like?.layoutManager = lm
        likeList.addAll(likeDao.getLikeListByBoxId(boxId))
        Log.i("search", likeList.size.toString())
        // TODO
        adapter = SimpleScpAdapter(this, likeList)
        rv_like?.adapter = adapter
        adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent()
                intent.putExtra("link", likeList[position]?.link)
                intent.setClass(this@LikeActivity, DetailActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun initToolbar() {
        baseToolbar = like_toolbar
        supportActionBar?.title = "收藏列表"
//        like_toolbar?.inflateMenu(R.menu.category_menu)
//        like_toolbar?.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.reverse -> {
//                    orderType = if (orderType == 0) 1 else 0
//                }
//            }
//            true
//        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.category_menu, menu)
//        return true
//    }
}
