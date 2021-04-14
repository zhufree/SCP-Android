package info.free.scp.view.random

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.bean.ScpModel
import info.free.scp.databinding.ItemRandomBinding
import info.free.scp.view.detail.DetailActivity

class RandomAdapter : ListAdapter<ScpModel, RandomAdapter.RandomHolder>(RandomDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomHolder {
        return RandomHolder((ItemRandomBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)))
    }

    override fun onBindViewHolder(holder: RandomHolder, position: Int) {
        val scp = getItem(position)
        holder.apply {
            bind(createOnClickListener(scp, position), scp)
        }
    }

    private fun createOnClickListener(scp: ScpModel, pos: Int): View.OnClickListener {
        return View.OnClickListener { v ->
            val intent = Intent()
            intent.putExtra("link", scp.link)
            intent.putExtra("title", scp.title)
            intent.putExtra("read_type", 1)
            intent.putExtra("random_index", pos)
            val randomLinkList = currentList.map { it.link } as ArrayList
            val randomTitleList = currentList.map { it.title } as ArrayList
            intent.putStringArrayListExtra("random_link_list", randomLinkList)
            intent.putStringArrayListExtra("random_title_list", randomTitleList)
            intent.setClass(v.context, DetailActivity::class.java)
            (v.context as Activity).startActivity(intent)
        }
    }

    class RandomHolder(private val bd: ItemRandomBinding) : RecyclerView.ViewHolder(bd.root) {

        fun bind(listener: View.OnClickListener, scp: ScpModel) {
            bd.apply {
                title = scp.title
                clickListener = listener
                executePendingBindings()
            }
        }
    }

    class RandomDiffCallback : DiffUtil.ItemCallback<ScpModel>() {

        override fun areItemsTheSame(oldItem: ScpModel, newItem: ScpModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScpModel, newItem: ScpModel): Boolean {
            return oldItem.link == newItem.link
        }
    }
}