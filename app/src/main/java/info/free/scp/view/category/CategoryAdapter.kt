package info.free.scp.view.category

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.databinding.ItemCategoryBinding
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import org.jetbrains.anko.startActivity

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryAdapter(val saveType: Int) : ListAdapter<String, CategoryAdapter.CategoryHolder>(StringDiffCallback()) {
    private val holderList = mutableListOf<CategoryHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val newHolder = CategoryHolder(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val title = getItem(position)
        holder.apply {
            bind(createOnClickListener(title, position), title)
            itemView.tag = title
        }
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    private fun createOnClickListener(title: String, position: Int): View.OnClickListener {
        return View.OnClickListener {
            PreferenceUtil.addPoints(2)
            (it.context as Activity).startActivity<DocListActivity>("saveType"
                    to saveType,
                    "groupIndex" to position,
                    "extraType" to title
            )
        }
    }

    class CategoryHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, categoryTitle: String) {
            binding.apply {
                clickListener = listener
                title = categoryTitle
                executePendingBindings()
            }
        }

        fun refreshTheme() {
            binding.tvCategoryTitle.setTextColor(ThemeUtil.darkText)
            binding.clCategoryItem.background = ThemeUtil.getDrawable(binding.clCategoryItem.context, R.drawable.bg_entry_box)
        }
    }

    private class StringDiffCallback : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(
                oldItem: String,
                newItem: String
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
                oldItem: String,
                newItem: String
        ): Boolean {
            return oldItem == newItem
        }
    }
}
