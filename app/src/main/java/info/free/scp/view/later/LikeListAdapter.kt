package info.free.scp.view.later

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.R
import info.free.scp.bean.ScpLikeModel
import info.free.scp.databinding.ItemLikeBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity

class LikeListAdapter : ListAdapter<ScpLikeModel, LikeListAdapter.LikeHolder>(LaterDiffCallback()) {

    var onLongClick: (pos: Int, item: ScpLikeModel) -> Unit = { _, _ -> }
    val holderList: MutableList<LikeHolder> = emptyList<LikeHolder>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeHolder {
        val newHolder = LikeHolder(ItemLikeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    override fun onBindViewHolder(holder: LikeHolder, position: Int) {
        val like = getItem(position)
        holder.apply {
            bind(createOnClickListener(like), createOnLongClickListener(position, like), like)
            itemView.tag = like
        }
    }

    private fun createOnClickListener(like: ScpLikeModel): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            intent.putExtra("link", like.link)
            intent.putExtra("title", like.link)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivity(intent)
        }
    }

    private fun createOnLongClickListener(pos: Int, like: ScpLikeModel): View.OnLongClickListener {
        return View.OnLongClickListener {
            onLongClick(pos, like)
            true
        }
    }

    class LikeHolder(private val binding: ItemLikeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, longListener: View.OnLongClickListener, item: ScpLikeModel) {
            binding.apply {
                clickListener = listener
                longClickListener = longListener
                like = item
                executePendingBindings()
            }
        }

        fun refreshTheme() {
            binding.clLikeContainer.background =
                    ThemeUtil.getDrawable(binding.clLikeContainer.context, R.drawable.bg_entry_box)
            binding.tvLikeItemTitle.setTextColor(ThemeUtil.darkText)
        }
    }

    private class LaterDiffCallback : DiffUtil.ItemCallback<ScpLikeModel>() {

        override fun areItemsTheSame(
                oldItem: ScpLikeModel,
                newItem: ScpLikeModel
        ): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(
                oldItem: ScpLikeModel,
                newItem: ScpLikeModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}