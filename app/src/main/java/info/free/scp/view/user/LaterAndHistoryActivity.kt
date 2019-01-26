package info.free.scp.view.user

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.View
import info.free.scp.R
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDao
import info.free.scp.util.EventUtil
import info.free.scp.util.EventUtil.clickHistoryList
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.search.SimpleScpAdapter
import kotlinx.android.synthetic.main.activity_like.*

/**
 * 待读列表
 */
class LaterAndHistoryActivity : BaseActivity() {
    private var viewType = -1
        set(value) {
            field = value
            viewItemList.clear()
            viewItemList.addAll(ScpDao.getInstance().getViewListByTypeAndOrder(value, orderType))
            supportActionBar?.title = if (value == HISTORY_TYPE) "历史阅读记录" else "待读列表"
            if (value == HISTORY_TYPE) {
                EventUtil.onEvent(this, clickHistoryList)
            }
            adapter?.notifyDataSetChanged()
        }
    val viewItemList = emptyList<SimpleScp?>().toMutableList()
    var adapter : SimpleScpAdapter? = null
    private var orderType = 0 // 0 時間正序，倒序
        set(value) {
            field = value
            viewItemList.clear()
            viewItemList.addAll(ScpDao.getInstance().getViewListByTypeAndOrder(viewType, value))

            adapter?.notifyDataSetChanged()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)
        initToolbar()

        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_like?.layoutManager = lm
        adapter = SimpleScpAdapter(this, viewItemList)
        rv_like?.adapter = adapter
        adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent()
                intent.putExtra("link", viewItemList[position]?.link)
                intent.setClass(this@LaterAndHistoryActivity, DetailActivity::class.java)
                startActivity(intent)
            }
        }
        viewType = intent?.getIntExtra("view_type", 0)?:0
    }

    override fun onResume() {
        super.onResume()
        viewItemList.clear()
        viewItemList.addAll(ScpDao.getInstance().getViewListByTypeAndOrder(viewType, orderType))
        adapter?.notifyDataSetChanged()
    }

    private fun initToolbar() {
        setSupportActionBar(like_toolbar)
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
