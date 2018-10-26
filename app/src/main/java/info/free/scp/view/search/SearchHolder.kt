package info.free.scp.view.search

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_search.view.*

/**
 * Created by zhufree on 2018/10/25.
 * 搜索结果item
 */

class SearchHolder(view: View) : RecyclerView.ViewHolder(view){

    fun setData(title: String?) {
        itemView?.tv_search_title?.text = title
    }
}