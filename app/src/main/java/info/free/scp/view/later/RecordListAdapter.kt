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
import info.free.scp.bean.ScpRecordModel
import info.free.scp.databinding.ItemLaterBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity

class RecordListAdapter : ListAdapter<ScpRecordModel, RecordListAdapter.RecordHolder>(LaterDiffCallback()) {
    var onLongClick: (item: ScpRecordModel) -> Unit = { _ -> }
    val holderList: MutableList<RecordHolder> = emptyList<RecordHolder>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordHolder {
        val newHolder = RecordHolder(ItemLaterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        val record = getItem(position)
        holder.apply {
            bind(createOnClickListener(record), createOnLongClickListener(record), record)
            itemView.tag = record
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

    private fun createOnLongClickListener(record: ScpRecordModel): View.OnLongClickListener {
        return View.OnLongClickListener {
            onLongClick(record)
            true
        }
    }

    class RecordHolder(private val binding: ItemLaterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, longListener: View.OnLongClickListener, item: ScpRecordModel) {
            binding.apply {
                clickListener = listener
                longClickListener = longListener
                record = item
                executePendingBindings()
            }
        }

        fun refreshTheme() {
            binding.clLaterContainer.background = ThemeUtil.getDrawable(binding.clLaterContainer.context,
                    R.drawable.bg_entry_box)
            binding.tvHistoryItemTitle.setTextColor(ThemeUtil.darkText)
            binding.tvHistoryItemTime.setTextColor(ThemeUtil.mediumText)
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