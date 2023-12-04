package info.free.scp.view.later

import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.bean.ScpLikeModel
import info.free.scp.databinding.ActivityLikeBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector

/**
 * 一个收藏夹点开的收藏列表
 */
class LikeListActivity : BaseActivity() {
    private lateinit var binding: ActivityLikeBinding
    val orderedList = emptyList<ScpLikeModel>().toMutableList()
    val reverseList = emptyList<ScpLikeModel>().toMutableList()
    val likeList = emptyList<ScpLikeModel>().toMutableList()
    val adapter by lazy {
        LikeListAdapter()
    }
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
        binding.likeToolbar?.inflateMenu(R.menu.category_menu)
        binding.likeToolbar?.setOnMenuItemClickListener {
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

        binding.rvLike?.adapter = adapter
        adapter.submitList(likeList)

        adapter.onLongClick = { pos, item ->
            val selectOption = listOf("删除这条", "转移到其他收藏夹", "我手滑了")
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
                                likeDao.deleteLike(item)
                                likeList.removeAt(pos)
                                orderedList.remove(item)
                                reverseList.remove(item)
                                adapter.notifyDataSetChanged()
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
                            item.boxId = selectBox.id
                            likeDao.save(item)
                            if (item.boxId != boxId) {
                                likeList.removeAt(pos)
                                orderedList.remove(item)
                                reverseList.remove(item)
                                adapter.notifyDataSetChanged()
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }
}
