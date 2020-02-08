package info.free.scp.view.like

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import info.free.scp.R
import info.free.scp.bean.ScpLikeModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.user.SimpleScpAdapter
import kotlinx.android.synthetic.main.activity_like.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector

class LikeActivity : BaseActivity() {
    val orderedList = emptyList<ScpLikeModel>().toMutableList()
    val reverseList = emptyList<ScpLikeModel>().toMutableList()
    val likeList = emptyList<ScpLikeModel>().toMutableList()
    var adapter: SimpleScpAdapter? = null
    val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private var orderType = 0 // 0 默认顺序 1 按编号
        set(value) {
            field = value
            likeList.clear()
            if (value == 1) {
                likeList.addAll(reverseList)
            } else {
                likeList.addAll(orderedList)
            }
            adapter?.notifyDataSetChanged()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        val boxId = intent?.getIntExtra("box_id", 0) ?: 0
        val boxName = intent?.getStringExtra("box_name") ?: "收藏列表"

        baseToolbar = like_toolbar
        supportActionBar?.title = boxName
        like_toolbar?.inflateMenu(R.menu.category_menu)
        like_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    orderType = if (orderType == 0) 1 else 0
                }
            }
            true
        }

        val lm = LinearLayoutManager(this, VERTICAL, false)
        rv_like?.layoutManager = lm
        if (boxId == 1) {
            orderedList.addAll(likeDao.getLikeListByBoxId(0))
        }
        orderedList.addAll(likeDao.getLikeListByBoxId(boxId))
        reverseList.addAll(orderedList.sortedBy { it.title })
        likeList.addAll(orderedList)
        adapter = SimpleScpAdapter(this, likeList)
        rv_like?.adapter = adapter
        adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent()
                intent.putExtra("link", likeList[position].link)
                intent.setClass(this@LikeActivity, DetailActivity::class.java)
                startActivity(intent)
            }
        }

        adapter?.mOnItemLongClickListener = object : BaseAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View, position: Int) {
                val selectOption = listOf("删除这条", "转移到其他收藏夹", "我手滑了")
                val selectItem = likeList[position]
                selector("Notice", selectOption) { out, which ->
                    val field = out.javaClass.superclass?.getDeclaredField(
                            "mShowing")
                    field?.isAccessible = true
                    // 将mShowing变量设为false，表示对话框已关闭
                    field?.set(out, false)
                    when (which) {
                        0 -> {
                            alert("Notice") {
                                positiveButton("确定删除？") {
                                    likeDao.deleteLike(selectItem)
                                    likeList.removeAt(position)
                                    orderedList.remove(selectItem)
                                    reverseList.remove(selectItem)
                                    adapter?.notifyDataSetChanged()
                                    field?.set(out, true)
                                    out.dismiss()
                                }
                                negativeButton("我手滑了") { field?.set(out, true) }
                            }.show()
                        }
                        1 -> {
                            val likeBox = likeDao.getLikeBox()
                            val changeOption = likeBox.map { it.name }
                            selector("选择转移收藏夹", changeOption) { one, i ->
                                val selectBox = likeBox[i]
                                selectItem.boxId = selectBox.id
                                likeDao.save(selectItem)
                                if (selectItem.boxId != boxId) {
                                    likeList.removeAt(position)
                                    orderedList.remove(selectItem)
                                    reverseList.remove(selectItem)
                                    adapter?.notifyDataSetChanged()
                                }
                                field?.set(out, true)
                                out.dismiss()
                            }
                        }
                        2 -> {
                            field?.set(out, true)
                            out.dismiss()
                        }
                    }
                }
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }
}
