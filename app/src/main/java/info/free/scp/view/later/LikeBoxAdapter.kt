package info.free.scp.view.later

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpLikeBox
import info.free.scp.databinding.ItemLikeBoxBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils

class LikeBoxAdapter : ListAdapter<ScpLikeBox, LikeBoxAdapter.LikeBoxHolder>(LikeBoxDiffCallback()) {
    private val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val holderList = mutableListOf<LikeBoxHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeBoxHolder {
        val newHolder = LikeBoxHolder(
            ItemLikeBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        holderList.add(newHolder)
        return newHolder
    }

    override fun onBindViewHolder(holder: LikeBoxHolder, position: Int) {
        val likeBox = getItem(position)
        holder.apply {
            bind(createOnClickListener(likeBox), createOnLongClickListener(likeBox), likeBox)
            itemView.tag = likeBox
        }
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    private fun createOnClickListener(box: ScpLikeBox): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent(it.context, LikeListActivity::class.java)
                .putExtra("box_id", box.id)
                .putExtra("box_name", box.name)
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
        val activity = ScpApplication.currentActivity ?: return
        val selectOption = arrayOf("删除收藏夹并清空其中收藏", "删除收藏夹并转移到默认收藏夹", "重命名收藏夹", "取消")
        AlertDialog.Builder(activity)
            .setTitle("Notice")
            .setItems(selectOption) { dialog, which ->
                when (which) {
                    0 -> showDeleteAndClearDialog(activity, id)
                    1 -> showDeleteAndMoveDialog(activity, id)
                    2 -> showRenameDialog(activity, id)
                    3 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun showDeleteAndClearDialog(activity: Activity, id: Int) {
        AlertDialog.Builder(activity)
            .setTitle("Notice")
            .setMessage("确定删除该收藏夹，并清空其中的收藏内容吗？")
            .setPositiveButton("确定删除") { _, _ ->
                val likeList = likeDao.getLikeListByBoxId(id)
                likeList.forEach {
                    it.boxId = 0
                    it.like = false
                }
                likeDao.saveAll(likeList)
                likeDao.deleteLikeBoxById(id)
                updateList()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteAndMoveDialog(activity: Activity, id: Int) {
        AlertDialog.Builder(activity)
            .setTitle("Notice")
            .setMessage("确定删除该收藏夹，并把内容转移到默认收藏夹吗？")
            .setPositiveButton("确定删除") { _, _ ->
                val likeList = likeDao.getLikeListByBoxId(id)
                likeList.forEach {
                    it.boxId = 1
                }
                likeDao.saveAll(likeList)
                likeDao.deleteLikeBoxById(id)
                updateList()
            }
            .setNegativeButton("取消", null)
            .show()
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

    private fun showRenameDialog(activity: Activity, boxId: Int) {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                Utils.dp2px(16, activity),
                Utils.dp2px(16, activity),
                Utils.dp2px(16, activity),
                Utils.dp2px(16, activity)
            )
        }
        val titleView = TextView(activity).apply {
            text = "输入新的收藏夹名"
            setTextColor(ThemeUtil.darkText)
            textSize = 18f
        }
        val input = EditText(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            isSingleLine = true
            setText(likeDao.getLikeBoxById(boxId).name)
        }
        container.addView(titleView)
        container.addView(input)

        AlertDialog.Builder(activity)
            .setTitle("Notice")
            .setView(container)
            .setPositiveButton("确定") { _, _ ->
                val likeBox = likeDao.getLikeBoxById(boxId)
                val newName = input.text?.toString()?.trim().orEmpty()
                if (newName.isNotEmpty()) {
                    likeBox.name = newName
                    likeDao.saveLikeBox(likeBox)
                    updateList()
                }
            }
            .setNegativeButton("取消", null)
            .show()
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

        fun refreshTheme() {
            binding.clLikeBoxContainer.setBackgroundColor(ThemeUtil.itemBg)
            binding.tvBoxTitle.setTextColor(ThemeUtil.darkText)
        }
    }

    private class LikeBoxDiffCallback : DiffUtil.ItemCallback<ScpLikeBox>() {
        override fun areItemsTheSame(oldItem: ScpLikeBox, newItem: ScpLikeBox): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScpLikeBox, newItem: ScpLikeBox): Boolean {
            return oldItem == newItem
        }
    }
}
