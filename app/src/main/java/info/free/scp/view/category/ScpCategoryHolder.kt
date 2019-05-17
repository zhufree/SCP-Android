package info.free.scp.view.category

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.bean.SimpleScp
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.*
import info.free.scp.util.EventUtil.addLater
import kotlinx.android.synthetic.main.item_category.view.*
import org.jetbrains.anko.alert

/**
 * 列表的item
 * 有读过和收藏logo
 * 有加入待读按钮
 * TODO 样式待优化
 * Created by zhufree on 2018/8/22.
 */

class ScpCategoryHolder(view: View) : RecyclerView.ViewHolder(view){
    private val categoryHeight = PreferenceUtil.getCategoryHeight()
    private val categoryInterval = PreferenceUtil.getCategoryInterval()
    private var mContext: Context? = null

    init {
        val rlLp = itemView.cl_category_item.layoutParams as FrameLayout.LayoutParams
        rlLp.height = Utils.dp2px(categoryHeight)
        val lp = itemView.cv_category_item.layoutParams as RecyclerView.LayoutParams
        lp.topMargin = Utils.dp2px(categoryInterval)
        lp.bottomMargin = Utils.dp2px(categoryInterval/2)
        itemView.btn_read_later?.visibility = VISIBLE
        mContext = itemView.context
    }

    fun setData(model: ScpModel?, laterViewList: MutableList<ScpRecordModel>) {
        if (model == null) return
        itemView.tv_scp_title?.text = model.title
//                if (model.notFound == 1 && !model.title.contains("拒绝访问")
//                && !model.title.contains("禁止访问"))
//            "${model.title}[禁止访问]" else model.title
        itemView.iv_like_star?.visibility = if (model.like == 1) VISIBLE else GONE
        itemView.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
        itemView.iv_read_label.setOnClickListener {
            itemView.context.alert("是否确定取消已读这篇文档？", "取消已读") {
                positiveButton("确定"){
                    EventUtil.onEvent(mContext, EventUtil.cancelRead, model.link)
                    var scpInfo = AppInfoDatabase.getInstance().likeAndReadDao().getInfoByLink(model.link)
                    if (scpInfo == null) {
                        scpInfo = ScpLikeModel(model.link, model.title, false, false)
                    }
                    scpInfo.hasRead = !scpInfo.hasRead
                    AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
                    itemView.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
                }
                negativeButton("我手滑了"){}
            }.show()
//            AlertDialog.Builder(mContext)
//                    .setTitle("取消已读")
//                    .setMessage("是否确定取消已读这篇文档？")
//                    .setPositiveButton("确定") { _, _ ->
//                        EventUtil.onEvent(mContext, EventUtil.cancelRead, model.link)
//                        model.hasRead = if (model.hasRead == 0) 1 else 0
//                        ScpDataHelper.getInstance().insertLikeAndReadInfo(model)
//                        itemView.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
//                    }
//                    .setNegativeButton("我手滑了") {dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    .create().show()
        }
        var isInLaterViewList = model.link in laterViewList.map { it.link }
        if (isInLaterViewList) {
            itemView.btn_read_later.setBackgroundColor(ThemeUtil.clickedBtn)
        } else {
            itemView.btn_read_later.setBackgroundColor(ThemeUtil.unClickBtn)
        }
        itemView.btn_read_later.setOnClickListener {
            if (isInLaterViewList) {
                AppInfoDatabase.getInstance().readRecordDao().delete(model.link, LATER_TYPE)
                isInLaterViewList = false
                it.setBackgroundColor(ThemeUtil.unClickBtn)
            } else {
                ScpDataHelper.getInstance().insertViewListItem(model.link, model.title, LATER_TYPE)
                Toaster.show("已加入待读列表")
                EventUtil.onEvent(mContext, addLater)
                isInLaterViewList = true
                it.setBackgroundColor(ThemeUtil.clickedBtn)
            }
        }
    }
}
