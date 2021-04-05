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
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.ScpApplication
import info.free.scp.bean.*
import info.free.scp.databinding.ItemCategoryBinding
import info.free.scp.databinding.ItemDocBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.random.RandomActivity
import kotlinx.android.synthetic.main.item_doc.view.*
import org.jetbrains.anko.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter : ListAdapter<ScpModel, ScpAdapter.ScpHolder>(ScpDiffCallback()) {
    var currentScrollPosition = -1
    val likeReadDao = AppInfoDatabase.getInstance().likeAndReadDao()
    val recordDao = AppInfoDatabase.getInstance().readRecordDao()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScpHolder {
        return ScpHolder(ItemDocBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ScpHolder, position: Int) {
        val scp = getItem(position)
        holder.apply {
            bind(createOnClickListener(scp, position), createOnLongClickListener(scp, holder.itemView.context), scp)
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
            intent.putExtra("index", scp.index)
            intent.putExtra("scp_type", scp.scpType)
            intent.putExtra("item_type", if (scp is ScpCollectionModel) 1 else 0)
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)

        }
    }

    private var laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)

    private fun createOnLongClickListener(scp: ScpModel, context: Context): View.OnLongClickListener {
        return View.OnLongClickListener {
            val operationList = mutableListOf<String>()
//            if (likeReadDao.getLikeByLink(scp.link) == true) {
//                operationList.add("取消收藏")
//            } else {
//                operationList.add("收藏文档")
//            }
            if (likeReadDao.getHasReadByLink(scp.link) == true) {
                operationList.add("取消已读")
            } else {
                operationList.add("标记已读")
            }
            var isInLaterViewList = scp.link in laterViewList.map { it.link }
            if (isInLaterViewList) {
                operationList.add("取消待读")
            } else {
                operationList.add("添加待读")
            }
            context.selector("", operationList) { _, i ->
                when (i) {
                    0 -> {
                        // 更改已读
                        var scpInfo = likeReadDao.getInfoByLink(scp.link)
                        if (scpInfo == null) {
                            scpInfo = ScpLikeModel(scp.link, scp.title, like = false, hasRead = false, boxId = 0)
                        }
                        scpInfo.hasRead = !scpInfo.hasRead
                        likeReadDao.save(scpInfo)
                    }
                    1 -> {
                        // 更改待读
                        if (isInLaterViewList) {
                            AppInfoDatabase.getInstance().readRecordDao().delete(scp.link, LATER_TYPE)
                            isInLaterViewList = false
                        } else {
                            ScpDataHelper.getInstance().insertViewListItem(scp.link, scp.title, LATER_TYPE)
                            ScpApplication.currentActivity?.toast("已加入待读列表")
                            EventUtil.onEvent(context, EventUtil.addLater)
                            isInLaterViewList = true
                        }
                    }
                }
            }
            true
        }
    }

    class ScpHolder(private val binding: ItemDocBinding) : RecyclerView.ViewHolder(binding.root) {
        private val categoryHeight = PreferenceUtil.getCategoryHeight()
        private val categoryInterval = PreferenceUtil.getCategoryInterval()
        val likeReadDao = AppInfoDatabase.getInstance().likeAndReadDao()
        fun bind(listener: View.OnClickListener, longListener: View.OnLongClickListener, item: ScpModel) {
            binding.apply {
                clickListener = listener
                longClickListener = longListener
                scp = item
                executePendingBindings()
//                val rlLp = clCategoryItem.layoutParams as FrameLayout.LayoutParams
//                rlLp.height = Utils.dp2px(categoryHeight)
//                val lp = cvCategoryItem.layoutParams as RecyclerView.LayoutParams
//                lp.topMargin = Utils.dp2px(categoryInterval)
//                lp.bottomMargin = Utils.dp2px(categoryInterval / 2)
//                itemView.btn_read_later?.visibility = View.VISIBLE
                setReadData(this, item)
            }
        }

        private fun setReadData(binding: ItemDocBinding, model: ScpModel?) {
            if (model == null) return
            if (likeReadDao.getLikeByLink(model.link) == true) {
                // add like icon
            }
            if (likeReadDao.getHasReadByLink(model.link) == true) {
                // add read icon
            }
//            binding.ivReadLabel.setOnClickListener {
//                itemView.context.alert("是否确定取消已读这篇文档？", "取消已读") {
//                    positiveButton("确定") {
//                        EventUtil.onEvent(itemView.context, EventUtil.cancelRead, model.link)
//                        var scpInfo = likeReadDao.getInfoByLink(model.link)
//                        if (scpInfo == null) {
//                            scpInfo = ScpLikeModel(model.link, model.title, like = false, hasRead = false, boxId = 0)
//                        }
//                        scpInfo.hasRead = !scpInfo.hasRead
//                        likeReadDao.save(scpInfo)
//                        itemView.iv_read_label?.visibility = if (scpInfo.hasRead) View.VISIBLE else View.GONE
//                    }
//                    negativeButton("我手滑了") {}
//                }.show()
//            }
//            var isInLaterViewList = model.link in laterViewList.map { it.link }
//            if (isInLaterViewList) {
//                binding.btnReadLater.setBackgroundColor(ThemeUtil.clickedBtn)
//            } else {
//                binding.btnReadLater.setBackgroundColor(ThemeUtil.unClickBtn)
//            }
//            binding.btnReadLater.setOnClickListener {
//                if (isInLaterViewList) {
//                    AppInfoDatabase.getInstance().readRecordDao().delete(model.link, LATER_TYPE)
//                    isInLaterViewList = false
//                    it.setBackgroundColor(ThemeUtil.unClickBtn)
//                } else {
//                    ScpDataHelper.getInstance().insertViewListItem(model.link, model.title, LATER_TYPE)
//                    ScpApplication.currentActivity?.toast("已加入待读列表")
//                    EventUtil.onEvent(itemView.context, EventUtil.addLater)
//                    isInLaterViewList = true
//                    it.setBackgroundColor(ThemeUtil.clickedBtn)
//                }
//            }
        }

        fun refreshTheme() {
            binding.tvDocTitle.setTextColor(ThemeUtil.darkText)
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
