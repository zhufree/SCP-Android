package info.free.scp.view.feed

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.bean.FeedModel
import info.free.scp.databinding.ItemFeedBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity
import org.jetbrains.anko.backgroundColor

class FeedAdapter : ListAdapter<FeedModel, FeedAdapter.FeedHolder>(FeedDiffCallback()) {

    val holderList: MutableList<FeedHolder> = emptyList<FeedHolder>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHolder {
        val newHolder = FeedHolder(ItemFeedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    override fun onBindViewHolder(holder: FeedHolder, position: Int) {
        val feed = getItem(position)
        holder.apply {
            bind(createOnClickListener(feed), feed)
            itemView.tag = feed
        }
    }

    private fun createOnClickListener(feed: FeedModel): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            intent.putExtra("link", feed.link)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivity(intent)
        }
    }

    class FeedHolder(private val binding: ItemFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: FeedModel) {
            binding.apply {
                clickListener = listener
                feed = item
                executePendingBindings()
            }
        }

        fun refreshTheme() {
            binding.clFeedContainer.backgroundColor = ThemeUtil.itemBg
            binding.tvFeedTitle.setTextColor(ThemeUtil.darkText)
        }
    }

    private class FeedDiffCallback : DiffUtil.ItemCallback<FeedModel>() {

        override fun areItemsTheSame(
                oldItem: FeedModel,
                newItem: FeedModel
        ): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(
                oldItem: FeedModel,
                newItem: FeedModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}