package info.free.scp.view.category

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.FeedModel
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.databinding.ItemCategoryBinding
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.item_category.view.*
import org.jetbrains.anko.backgroundColor

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter : ListAdapter<ScpModel, ScpAdapter.ScpHolder>(ScpDiffCallback()) {
    private var laterViewList = emptyList<ScpRecordModel>().toMutableList()
    var currentScrollPosition = -1

    init {
        laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)
    }

    val holderList: MutableList<ScpHolder> = emptyList<ScpHolder>().toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScpHolder {
        val newHolder = ScpHolder(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
    }

    override fun onBindViewHolder(holder: ScpHolder, position: Int) {
        val scp = getItem(position)
        holder.apply {
            bind(createOnClickListener(scp, position), scp)
            itemView.tag = scp
        }
    }

    private fun createOnClickListener(scp: ScpModel, position: Int): View.OnClickListener {
        return View.OnClickListener {
            currentScrollPosition = position

            PreferenceUtil.addPoints(2)
            val intent = Intent()
            intent.putExtra("link", scp.link)
            intent.putExtra("title", scp.title)
            intent.putExtra("scp_type", if (scp is ScpCollectionModel) 1 else 0)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)

        }
    }

    class ScpHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val categoryHeight = PreferenceUtil.getCategoryHeight()
        private val categoryInterval = PreferenceUtil.getCategoryInterval()

        fun bind(listener: View.OnClickListener, item: ScpModel) {
            binding.apply {
                clickListener = listener
                scp = item
                executePendingBindings()
                val rlLp = clCategoryItem.layoutParams as FrameLayout.LayoutParams
                rlLp.height = Utils.dp2px(categoryHeight)
                val lp = cvCategoryItem.layoutParams as RecyclerView.LayoutParams
                lp.topMargin = Utils.dp2px(categoryInterval)
                lp.bottomMargin = Utils.dp2px(categoryInterval / 2)
            }
        }

        fun refreshTheme() {
            binding.clCategoryItem.backgroundColor = ThemeUtil.itemBg
            binding.tvScpTitle.setTextColor(ThemeUtil.darkText)
        }
    }

    private class ScpDiffCallback : DiffUtil.ItemCallback<ScpModel>() {

        override fun areItemsTheSame(
                oldItem: ScpModel,
                newItem: ScpModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
                oldItem: ScpModel,
                newItem: ScpModel
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
