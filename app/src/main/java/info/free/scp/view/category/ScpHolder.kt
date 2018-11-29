package info.free.scp.view.category

import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpHolder(view: View) : RecyclerView.ViewHolder(view){
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val categoryHeight = PreferenceUtil.getCategoryHeight()
    private val categoryInterval = PreferenceUtil.getCategoryInterval()

    init {
        itemView.rl_category_item.minimumHeight = categoryHeight
        val lp = itemView.cv_category_item.layoutParams as RecyclerView.LayoutParams
        lp.topMargin = Utils.dp2px(categoryInterval)
        lp.bottomMargin = Utils.dp2px(categoryInterval/2)
    }

    fun setData(model: ScpModel) {
        itemView?.tvScpTitle?.text = if (model.notFound == "true" && !model.title.contains("拒绝访问"))
            "${model.title}[禁止访问]" else model.title
        itemView?.iv_like_star?.visibility = if (model.like == 1) VISIBLE else GONE
        itemView?.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
        itemView?.iv_read_label.setOnClickListener {
            AlertDialog.Builder(itemView?.context)
                    .setTitle("取消已读")
                    .setMessage("是否确定取消已读这篇文档？")
                    .setPositiveButton("确定") { _, _ ->
                        EventUtil.onEvent(itemView?.context, EventUtil.cancelRead, model.link)
                        model.hasRead = if (model.hasRead == 0) 1 else 0
                        ScpDao.getInstance().insertLikeAndReadInfo(model)
                        itemView?.iv_read_label?.visibility = if (model.hasRead == 1) VISIBLE else GONE
                    }
                    .setNegativeButton("我手滑了") {dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()

        }
    }
}
