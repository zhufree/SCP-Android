package info.free.scp.view.search

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.databinding.ItemSearchBinding
import info.free.scp.view.detail.DetailActivity

class SearchResultAdapter : ListAdapter<ScpModel, SearchResultAdapter.SearchHolder>(LaterDiffCallback()) {
    val holderList: MutableList<SearchHolder> = emptyList<SearchHolder>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val newHolder = SearchHolder(ItemSearchBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        val search = getItem(position)
        holder.apply {
            bind(createOnClickListener(search), search)
            itemView.tag = search
        }
    }

    private fun createOnClickListener(search: ScpModel): View.OnClickListener {
        return View.OnClickListener {
            val intent = Intent()
            intent.putExtra("link", search.link)
            intent.putExtra("title", search.link)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivity(intent)
        }
    }


    class SearchHolder(private val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: ScpModel) {
            binding.apply {
                clickListener = listener
                search = item
                executePendingBindings()
            }
        }

        fun refreshTheme() {
            //TODO
//            binding.clFeedContainer.backgroundColor = ThemeUtil.itemBg
//            binding.tvFeedTitle.setTextColor(ThemeUtil.darkText)
        }
    }

    private class LaterDiffCallback : DiffUtil.ItemCallback<ScpModel>() {

        override fun areItemsTheSame(
                oldItem: ScpModel,
                newItem: ScpModel
        ): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(
                oldItem: ScpModel,
                newItem: ScpModel
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}