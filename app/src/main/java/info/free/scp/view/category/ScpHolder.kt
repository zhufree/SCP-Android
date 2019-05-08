package info.free.scp.view.category

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.ScpModel
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.*
import info.free.scp.util.EventUtil.addLater
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view){
    private val categoryHeight = PreferenceUtil.getCategoryHeight()
    private val categoryInterval = PreferenceUtil.getCategoryInterval()
    private var mContext: Context? = null

    init {
        val rlLp = itemView.rl_category_item.layoutParams as FrameLayout.LayoutParams
        rlLp.height = Utils.dp2px(categoryHeight)
        val lp = itemView.cv_category_item.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        lp.topMargin = Utils.dp2px(categoryInterval)
        lp.bottomMargin = Utils.dp2px(categoryInterval/2)
        itemView.btn_read_later?.visibility = VISIBLE
        mContext = itemView.context
    }

    fun setData(model: ScpModel?, laterViewList: MutableList<SimpleScp>) {
        if (model == null) return
//        itemView.tv_scp_title?.text = if (model.notFound == 1 && !model.title.contains("拒绝访问")
//                && !model.title.contains("禁止访问"))
//            "${model.title}[禁止访问]" else model.title
        itemView.iv_like_star?.visibility = if (model.like == 1) VISIBLE else GONE
        itemView.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
        itemView.iv_read_label.setOnClickListener {
            AlertDialog.Builder(mContext)
                    .setTitle("取消已读")
                    .setMessage("是否确定取消已读这篇文档？")
                    .setPositiveButton("确定") { _, _ ->
                        EventUtil.onEvent(mContext, EventUtil.cancelRead, model.link)
                        model.hasRead = if (model.hasRead == 0) 1 else 0
                        ScpDataHelper.getInstance().insertLikeAndReadInfo(model)
                        itemView.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
                    }
                    .setNegativeButton("我手滑了") {dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
        }
        var isInLaterViewList = model.link in laterViewList.map { it.link }
        if (isInLaterViewList) {
            itemView.btn_read_later.setBackgroundColor(ThemeUtil.clickedBtn)
        } else {
            itemView.btn_read_later.setBackgroundColor(ThemeUtil.unClickBtn)
        }
        itemView.btn_read_later.setOnClickListener {
            if (isInLaterViewList) {
                ScpDataHelper.getInstance().deleteViewListItem(model.link, LATER_TYPE)
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
