package info.free.scp.view.later

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.bean.ScpRecordModel
import info.free.scp.databinding.ItemLaterBinding
import info.free.scp.view.detail.DetailActivity

class LaterListAdapter : ListAdapter<ScpRecordModel, LaterListAdapter.LaterHolder>(LaterDiffCallback()) {

    val holderList: MutableList<LaterHolder> = emptyList<LaterHolder>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaterHolder {
        val newHolder = LaterHolder(ItemLaterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    override fun onBindViewHolder(holder: LaterHolder, position: Int) {
        val feed = getItem(position)
        holder.apply {
            bind(createOnClickListener(feed), feed)
            itemView.tag = feed
        }
    }

    private fun createOnClickListener(record: ScpRecordModel): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            intent.putExtra("link", record.link)
            intent.putExtra("title", record.link)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivity(intent)
        }
    }

    class LaterHolder(private val binding: ItemLaterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: ScpRecordModel) {
            binding.apply {
                clickListener = listener
                record = item
                executePendingBindings()
            }
        }

        fun refreshTheme() {
            //TODO
//            binding.clFeedContainer.backgroundColor = ThemeUtil.itemBg
//            binding.tvFeedTitle.setTextColor(ThemeUtil.darkText)
        }
    }

    private class LaterDiffCallback : DiffUtil.ItemCallback<ScpRecordModel>() {

        override fun areItemsTheSame(
                oldItem: ScpRecordModel,
                newItem: ScpRecordModel
        ): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(
                oldItem: ScpRecordModel,
                newItem: ScpRecordModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}