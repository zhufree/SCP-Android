package info.free.scp.view.later

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.bean.ScpLikeModel
import info.free.scp.databinding.ActivityLikeBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.view.base.BaseActivity

/**
 * 一个收藏夹点开的收藏列表。
 */
class LikeListActivity : BaseActivity() {
    private lateinit var binding: ActivityLikeBinding
    private val orderedList = emptyList<ScpLikeModel>().toMutableList()
    private val reverseList = emptyList<ScpLikeModel>().toMutableList()
    private val likeList = emptyList<ScpLikeModel>().toMutableList()
    private val adapter by lazy {
        LikeListAdapter()
    }
    private val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private var orderType = 0 // 0 默认顺序，1 按标题排序
        set(value) {
            field = value
            likeList.clear()
            if (value == 1) {
                likeList.addAll(reverseList)
            } else {
                likeList.addAll(orderedList)
            }
            adapter.notifyDataSetChanged()
        }
    private val viewModel by lazy {
        ViewModelProvider(this)[LaterViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val boxId = intent?.getIntExtra("box_id", 0) ?: 0
        val boxName = intent?.getStringExtra("box_name") ?: "收藏列表"

        baseToolbar = binding.likeToolbar
        supportActionBar?.title = boxName
        binding.likeToolbar.inflateMenu(R.menu.category_menu)
        binding.likeToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    orderType = if (orderType == 0) 1 else 0
                }
            }
            true
        }

        if (boxId == 1) {
            orderedList.addAll(viewModel.getLikeListByBoxId(0))
        }
        orderedList.addAll(viewModel.getLikeListByBoxId(boxId))
        reverseList.addAll(orderedList.sortedBy { it.title })
        likeList.addAll(orderedList)

        binding.rvLike.adapter = adapter
        adapter.submitList(likeList)

        adapter.onLongClick = { pos, item ->
            showItemOperationDialog(boxId, pos, item)
        }
    }

    private fun showItemOperationDialog(boxId: Int, pos: Int, item: ScpLikeModel) {
        val options = arrayOf("删除这条", "转移到其他收藏夹", "取消")
        AlertDialog.Builder(this)
            .setTitle("Notice")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showDeleteConfirmDialog(pos, item)
                    1 -> showMoveDialog(boxId, pos, item)
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun showDeleteConfirmDialog(pos: Int, item: ScpLikeModel) {
        AlertDialog.Builder(this)
            .setTitle("Notice")
            .setPositiveButton("确定删除？") { _, _ ->
                likeDao.deleteLike(item)
                likeList.removeAt(pos)
                orderedList.remove(item)
                reverseList.remove(item)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showMoveDialog(boxId: Int, pos: Int, item: ScpLikeModel) {
        val likeBox = likeDao.getLikeBox()
        val changeOption = likeBox.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择转移收藏夹")
            .setItems(changeOption) { _, i ->
                val selectBox = likeBox[i]
                item.boxId = selectBox.id
                likeDao.save(item)
                if (item.boxId != boxId) {
                    likeList.removeAt(pos)
                    orderedList.remove(item)
                    reverseList.remove(item)
                    adapter.notifyDataSetChanged()
                }
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }
}
