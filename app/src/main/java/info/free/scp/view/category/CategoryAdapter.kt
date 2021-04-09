package info.free.scp.view.category

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.SCPConstants
import info.free.scp.databinding.ItemCategoryBinding
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.startActivity

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryAdapter(val saveType: Int) : ListAdapter<String, CategoryAdapter.CategoryHolder>(StringDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        return CategoryHolder(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        val title = getItem(position)
        holder.apply {
            bind(createOnClickListener(title, position), title)
            itemView.tag = title
        }
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
