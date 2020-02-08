package info.free.scp.view.category

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import info
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.ScpApplication
import info.free.scp.bean.*
import info.free.scp.databinding.ItemCategoryBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.item_category.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.toast

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter : ListAdapter<ScpModel, ScpAdapter.ScpHolder>(ScpDiffCallback()) {
    var currentScrollPosition = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScpHolder {
        return ScpHolder(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ScpHolder, position: Int) {
        val scp = getItem(position)
        holder.apply {
            bind(createOnClickListener(scp, position), scp)
            itemView.tag = scp
            refreshTheme()
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
        private var laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)

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
                itemView.btn_read_later?.visibility = View.VISIBLE
                setReadData(this, item, laterViewList)
            }
        }

        private fun setReadData(binding: ItemCategoryBinding, model: ScpModel?, laterViewList: MutableList<ScpRecordModel>) {
            if (model == null) return
            binding.ivLikeStar.visibility = if (AppInfoDatabase.getInstance().likeAndReadDao()
                            .getLikeByLink(model.link) == true) View.VISIBLE else View.GONE
            binding.ivReadLabel.visibility = if (AppInfoDatabase.getInstance().likeAndReadDao()
                            .getHasReadByLink(model.link) == true) View.VISIBLE else View.GONE
            binding.ivReadLabel.setOnClickListener {
                itemView.context.alert("是否确定取消已读这篇文档？", "取消已读") {
                    positiveButton("确定") {
                        EventUtil.onEvent(itemView.context, EventUtil.cancelRead, model.link)
                        var scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(model.link)
                        if (scpInfo == null) {
                            scpInfo = ScpLikeModel(model.link, model.title, like = false, hasRead = false, boxId = 0)
                        }
                        scpInfo.hasRead = !scpInfo.hasRead
                        AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
                        itemView.iv_read_label?.visibility = if (scpInfo.hasRead) View.VISIBLE else View.GONE
                    }
                    negativeButton("我手滑了") {}
                }.show()
            }
            var isInLaterViewList = model.link in laterViewList.map { it.link }
            if (isInLaterViewList) {
                binding.btnReadLater.setBackgroundColor(ThemeUtil.clickedBtn)
            } else {
                binding.btnReadLater.setBackgroundColor(ThemeUtil.unClickBtn)
            }
            binding.btnReadLater.setOnClickListener {
                if (isInLaterViewList) {
                    AppInfoDatabase.getInstance().readRecordDao().delete(model.link, LATER_TYPE)
                    isInLaterViewList = false
                    it.setBackgroundColor(ThemeUtil.unClickBtn)
                } else {
                    ScpDataHelper.getInstance().insertViewListItem(model.link, model.title, LATER_TYPE)
                    ScpApplication.currentActivity?.toast("已加入待读列表")
                    EventUtil.onEvent(itemView.context, EventUtil.addLater)
                    isInLaterViewList = true
                    it.setBackgroundColor(ThemeUtil.clickedBtn)
                }
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
