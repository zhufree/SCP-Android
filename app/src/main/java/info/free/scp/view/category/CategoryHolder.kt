package info.free.scp.view.category

import android.support.v7.widget.RecyclerView
import android.view.View
import info.free.scp.SCPConstants.SERIES
import info.free.scp.SCPConstants.SERIES_CN
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryHolder(view: View) : RecyclerView.ViewHolder(view){

    fun setData(data: Any, type: Int) {
        when (type) {
            SERIES -> {
                val start = data as Int
                itemView.tvScpTitle.text = "SCP系列${if (start == 0) start+1 else start}~${start+499}"
            }
            SERIES_CN -> {
                val start = data as Int
                itemView.tvScpTitle.text = "SCP-CN系列${if (start == 0) start+1 else start}~${start+99}"
            }
            else -> {
                itemView.tvScpTitle.text = data as String
            }
        }
    }
}
