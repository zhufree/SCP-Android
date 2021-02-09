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
            bind(createOnClickListener(scp), scp)
        }
    }

    private fun createOnClickListener(scp: ScpModel): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            // TODO
            intent.putExtra("link", scp.link)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivity(intent)
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