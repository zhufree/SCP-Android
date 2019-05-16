package info.free.scp.view.search

import android.content.Context
import android.view.View
import android.view.View.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Toaster
import kotlinx.android.synthetic.main.item_search.view.*

/**
 * 搜索结果：待读按钮
 * 待读列表：时间
 * 阅读历史：时间
 * 收藏列表
 * Created by zhufree on 2018/10/25.
 *
 */

class SearchHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var mContext: Context? = null

    init {
        mContext = itemView.context
    }

    fun setData(link: String, title: String, viewTime: String?, laterViewList: MutableList<SimpleScp>?) {
        itemView.tv_search_title?.text = title
        viewTime?.let {
            itemView.tv_view_time?.text = viewTime
        }
        laterViewList?.let {l->
            itemView.btn_search_read_later.visibility = VISIBLE
            var isInLaterViewList = link in l.map { it.link }
            if (isInLaterViewList) {
                itemView.btn_search_read_later.setBackgroundColor(ThemeUtil.clickedBtn)
            } else {
                itemView.btn_search_read_later.setBackgroundColor(ThemeUtil.unClickBtn)
            }
            itemView.btn_search_read_later.setOnClickListener {
                if (isInLaterViewList) {
                    ScpDataHelper.getInstance().deleteViewListItem(link, LATER_TYPE)
                    isInLaterViewList = false
                    it.setBackgroundColor(ThemeUtil.unClickBtn)
                } else {
                    ScpDataHelper.getInstance().insertViewListItem(link, title, LATER_TYPE)
                    Toaster.show("已加入待读列表")
                    isInLaterViewList = true
                    it.setBackgroundColor(ThemeUtil.clickedBtn)
                }
            }
        }
    }
}