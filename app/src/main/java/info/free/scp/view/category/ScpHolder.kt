package info.free.scp.view.category

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import info.free.scp.bean.ScpModel
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class ScpHolder(view: View) : RecyclerView.ViewHolder(view){

    fun setData(model: ScpModel) {
        itemView?.tvScpTitle?.text = model.title
        itemView?.iv_like_star?.visibility = if (model.like == 1) VISIBLE else GONE
    }
}
