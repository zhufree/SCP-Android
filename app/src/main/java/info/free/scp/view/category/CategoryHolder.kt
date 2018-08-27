package info.free.scp.view.category

import android.support.v7.widget.RecyclerView
import android.view.View
import info.free.scp.bean.ScpModel
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryHolder(view: View) : RecyclerView.ViewHolder(view){

    fun setData(model: ScpModel) {
        itemView.tvScpTitle.text = model.title
    }
}
