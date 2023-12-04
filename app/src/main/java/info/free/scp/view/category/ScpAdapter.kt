package info.free.scp.view.category

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.*
import info.free.scp.databinding.ItemDocBinding
import info.free.scp.databinding.LayoutBottomSheetBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity
import org.jetbrains.anko.*


/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpAdapter : ListAdapter<ScpModel, ScpAdapter.ScpHolder>(ScpDiffCallback()) {
    private var currentScrollPosition = -1
    private val likeReadDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val holderList = mutableListOf<ScpHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScpHolder {
        val newHolder = ScpHolder(ItemDocBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        holderList.add(newHolder)
        return newHolder
    }


    override fun onBindViewHolder(holder: ScpHolder, position: Int) {
        val scp = getItem(position)
        holder.apply {
            bind(createOnClickListener(scp, position), createOnMoreClickListener(scp, holder.itemView.context), scp)
            itemView.tag = scp
            refreshTheme()
        }
    }

    fun refreshTheme() {
        holderList.forEach { it.refreshTheme() }
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
            intent.setClass(it.context, DetailActivity::class.java)
            (it.context as Activity).startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)
        }
    }

    private var laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)

    private fun refreshOperationStatus(scp: ScpModel, sheetView: LayoutBottomSheetBinding) {
        if (likeReadDao.getHasReadByLink(scp.link) == true) {
            sheetView.tvAddRead.text = "取消已读"
            sheetView.ivAddReadIcon.imageResource = R.drawable.baseline_article_black_24dp
        } else {
            sheetView.tvAddRead.text = "标记已读"
            sheetView.ivAddReadIcon.imageResource = R.drawable.baseline_fact_check_black_24dp
        }
        laterViewList = ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, 0)
        val isInLaterViewList = scp.link in laterViewList.map { it.link }
        if (isInLaterViewList) {
            sheetView.tvAddLater.text = "取消待读"
            sheetView.ivAddLaterIcon.imageResource = R.drawable.baseline_task_alt_blue_300_24dp
        } else {
            sheetView.tvAddLater.text = "添加待读"
            sheetView.ivAddLaterIcon.imageResource = R.drawable.baseline_add_task_blue_300_24dp
        }
    }

    @SuppressLint("InflateParams")
    private fun createOnMoreClickListener(scp: ScpModel, context: Context): View.OnClickListener {
        return View.OnClickListener {
            val mBottomSheetDialog = BottomSheetDialog(context)
            val sheetView = LayoutBottomSheetBinding.inflate(context.layoutInflater)
            mBottomSheetDialog.setContentView(sheetView.root)
            mBottomSheetDialog.show()
            refreshOperationStatus(scp, sheetView)

            sheetView.ivAddReadIcon.setOnClickListener {
                var scpInfo = likeReadDao.getInfoByLink(scp.link)
                if (scpInfo == null) {
                    scpInfo =
                        ScpLikeModel(scp.link, scp.title, like = false, hasRead = false, boxId = 0)
                }
                scpInfo.hasRead = !scpInfo.hasRead
                likeReadDao.save(scpInfo)
                refreshOperationStatus(scp, sheetView)
            }
            var isInLaterViewList = scp.link in laterViewList.map { it.link }
            sheetView.ivAddLaterIcon.setOnClickListener {
                if (isInLaterViewList) {
                    AppInfoDatabase.getInstance().readRecordDao().delete(scp.link, LATER_TYPE)
                    isInLaterViewList = false
                } else {
                    ScpDataHelper.getInstance().insertViewListItem(scp.link, scp.title, LATER_TYPE)
                    EventUtil.onEvent(context, EventUtil.addLater)
                    isInLaterViewList = true
                }
                refreshOperationStatus(scp, sheetView)
            }
        }
    }

    class ScpHolder(private val binding: ItemDocBinding) : RecyclerView.ViewHolder(binding.root) {
        val likeReadDao = AppInfoDatabase.getInstance().likeAndReadDao()
        fun bind(listener: View.OnClickListener, longListener: View.OnClickListener, item: ScpModel) {
            binding.apply {
                clickListener = listener
                moreClickListener = longListener
                scp = item
                executePendingBindings()
                setReadData(this, item)
            }
        }

        private fun setReadData(binding: ItemDocBinding, model: ScpModel?) {
            if (model == null) return

            if (likeReadDao.getLikeByLink(model.link) == true) {
                binding.ivDocIcon.imageResource = R.drawable.ic_star_yellow_900_24dp
            } else {
                if (likeReadDao.getHasReadByLink(model.link) == true) {
                    // add read icon
                    binding.ivDocIcon.imageResource = R.drawable.baseline_fact_check_black_24dp
                    binding.tvDocTitle.setTextColor(ThemeUtil.mediumText)
                } else {
                    binding.ivDocIcon.imageResource = R.drawable.baseline_article_black_24dp
                    binding.tvDocTitle.setTextColor(ThemeUtil.darkText)
                }
            }
        }

        fun refreshTheme() {
            binding.tvDocTitle.setTextColor(ThemeUtil.darkText)
            binding.clDocItem.background = ThemeUtil.getDrawable(binding.clDocItem.context, R.drawable.bg_entry_box)
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
