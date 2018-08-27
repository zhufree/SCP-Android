package info.free.scp.view.category

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryHolder(view: View) : RecyclerView.ViewHolder(view){

    fun setData(start: Int) {
        itemView.tvScpTitle.text = if (start == 0) "${start+1}~${start+499}" else "$start~${start+499}"
    }
}
