package info.free.scp.view.later

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpLikeBox
import info.free.scp.databinding.ItemLikeBoxBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.ThemeUtil
import org.jetbrains.anko.*
import java.lang.reflect.Field

class LikeBoxAdapter : ListAdapter<ScpLikeBox, LikeBoxAdapter.LikeBoxHolder>(LikeBoxDiffCallback()) {
    val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeBoxHolder {
        return LikeBoxHolder(ItemLikeBoxBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LikeBoxHolder, position: Int) {
        val likeBox = getItem(position)
        holder.apply {
            bind(createOnClickListener(likeBox), createOnLongClickListener(likeBox), likeBox)
            itemView.tag = likeBox
        }
    }

    private fun createOnClickListener(box: ScpLikeBox): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            intent.putExtra("box_id", box.id)
            intent.putExtra("box_name", box.name)
            intent.setClass(it.context, LikeListActivity::class.java)
            (it.context as Activity).startActivity(intent)
        }
    }

    private fun createOnLongClickListener(box: ScpLikeBox): View.OnLongClickListener {
        return View.OnLongClickListener {
            onLongClick(box.id)
            true
        }
    }

    private fun onLongClick(id: Int) {
        if (id == 1) return
        val selectOption = listOf("删除收藏夹并删除其中收藏内容", "删除收藏夹，将其中内容转移到默认收藏夹", "重命名收藏夹", "我手滑了")
        ScpApplication.currentActivity?.selector("Notice", selectOption) { out, which ->
            val field = out.javaClass.superclass?.getDeclaredField(
                    "mShowing")
            field?.isAccessible = true
            // 将mShowing变量设为false，表示对话框已关闭
            field?.set(out, false)
            when (which) {
                0 -> {
                    ScpApplication.currentActivity?.alert("Notice") {
                        positiveButton("确定删除") {
                            // 获取其中所有收藏内容，取消收藏，删除收藏夹
                            val likeList = likeDao.getLikeListByBoxId(id)
                            likeList.forEach {
                                it.boxId = 0
                                it.like = false
                            }
                            likeDao.saveAll(likeList)
                            likeDao.deleteLikeBoxById(id)
                            field?.set(out, true)
                            out.dismiss()
                            updateList()
                        }
                        negativeButton("我手滑了") { field?.set(out, true) }
                    }?.show()
                }
                1 -> {
                    ScpApplication.currentActivity?.alert("Notice") {
                        positiveButton("确定删除") {
                            field?.set(out, true)
                            out.dismiss()
                            // 获取其中所有收藏内容，修改收藏夹id，删除收藏夹
                            val likeList = likeDao.getLikeListByBoxId(id)
                            likeList.forEach {
                                it.boxId = 1
                            }
                            likeDao.saveAll(likeList)
                            likeDao.deleteLikeBoxById(id)
                            updateList()
                        }
                        negativeButton("我手滑了") { field?.set(out, true) }
                    }?.show()
                }
                2 -> {
                    showInputAlert(out, field, id)
                }
                3 -> {
                    field?.set(out, true)
                    out.dismiss()
                }
            }

        }
    }

    private fun updateList() {
        val boxList = arrayListOf<ScpLikeBox>()
        boxList.addAll(likeDao.getLikeBox())
        if (boxList.isEmpty()) {
            val defaultBox = ScpLikeBox(0, "默认收藏夹")
            boxList.add(defaultBox)
            likeDao.saveLikeBox(defaultBox)
        }
        submitList(boxList)
    }

    private fun showInputAlert(out: DialogInterface, field: Field?, boxId: Int) {
        var input: EditText? = null
        ScpApplication.currentActivity?.alert {
            customView {
                linearLayout {
                    padding = dip(16)
                    orientation = LinearLayout.VERTICAL
                    textView("输入新的收藏夹名") {
                        textColor = ThemeUtil.darkText
                        textSize = 18f
                    }
                    input = editText {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        singleLine = true
                    }
                }
            }
            positiveButton("确定") {
                val likeBox = likeDao.getLikeBoxById(boxId)
                likeBox.name = input?.text?.toString() ?: likeBox.name
                likeDao.saveLikeBox(likeBox)
                updateList()
                field?.set(out, true)
                out.dismiss()
            }
            negativeButton("取消") {
                field?.set(out, true)
                out.dismiss()
            }

        }?.show()
    }

    class LikeBoxHolder(private val binding: ItemLikeBoxBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, onLongClickListener: View.OnLongClickListener, item: ScpLikeBox) {
            binding.apply {
                clickListener = listener
                box = item
                executePendingBindings()
                clLikeBoxContainer.setOnLongClickListener(onLongClickListener)
            }
        }
    }

    private class LikeBoxDiffCallback : DiffUtil.ItemCallback<ScpLikeBox>() {

        override fun areItemsTheSame(
                oldItem: ScpLikeBox,
                newItem: ScpLikeBox
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
                oldItem: ScpLikeBox,
                newItem: ScpLikeBox
        ): Boolean {
            return oldItem == newItem
        }
    }
}