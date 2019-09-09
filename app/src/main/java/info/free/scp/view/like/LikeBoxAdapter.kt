package info.free.scp.view.like

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.bean.ScpLikeBox
import info.free.scp.databinding.ItemFeedBinding
import info.free.scp.databinding.ItemLikeBoxBinding
import info.free.scp.view.detail.DetailActivity

class LikeBoxAdapter : ListAdapter<ScpLikeBox, LikeBoxAdapter.LikeBoxHolder>(LikeBoxDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeBoxHolder {
        return LikeBoxHolder(ItemLikeBoxBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LikeBoxHolder, position: Int) {
        val feed = getItem(position)
        holder.apply {
            bind(createOnClickListener(feed), feed)
            itemView.tag = feed
        }
    }

    private fun createOnClickListener(box: ScpLikeBox): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            intent.putExtra("box_id", box.id)
            intent.setClass(it.context, LikeActivity::class.java)
            (it.context as Activity).startActivity(intent)
        }
    }

    class LikeBoxHolder(private val binding: ItemLikeBoxBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: ScpLikeBox) {
            binding.apply {
                clickListener = listener
                box = item
                executePendingBindings()
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