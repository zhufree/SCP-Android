package info.free.scp.view.search

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.View.VISIBLE
import info.free.scp.R
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.bean.ScpModel
import info.free.scp.bean.SimpleScp
import info.free.scp.db.ScpDao
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Toaster
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.item_search.view.*

/**
 * Created by zhufree on 2018/10/25.
 * 搜索结果item
 */

class SearchHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
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
                    ScpDao.getInstance().deleteViewListItem(link, LATER_TYPE)
                    isInLaterViewList = false
                    it.setBackgroundColor(ThemeUtil.unClickBtn)
                } else {
                    ScpDao.getInstance().insertViewListItem(link, title, LATER_TYPE)
                    Toaster.show("已加入待读列表")
                    isInLaterViewList = true
                    it.setBackgroundColor(ThemeUtil.clickedBtn)
                }

            }
        }
    }
}