package info.free.scp.view.category

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryHolder(view: View) : RecyclerView.ViewHolder(view){
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val categoryHeight = PreferenceUtil.getCategoryHeight()
    private val categoryInterval = PreferenceUtil.getCategoryInterval()

    init {
        val rlLp = itemView.rl_category_item.layoutParams as FrameLayout.LayoutParams
        rlLp.height = Utils.dp2px(categoryHeight)
        val lp = itemView.cv_category_item.layoutParams as RecyclerView.LayoutParams
        lp.topMargin = Utils.dp2px(categoryInterval)
        lp.bottomMargin = Utils.dp2px(categoryInterval/2)
    }
    fun setData(data: Any, type: Int) {
        when (type) {
            SERIES -> {
                val start = data as Int
                itemView.tvScpTitle.text = "SCP系列${start+1}~${start+categoryCount}"
            }
            SERIES_CN -> {
                val start = data as Int
                itemView.tvScpTitle.text = "SCP-CN系列${start+1}~${start+categoryCount}"
            }
            else -> {
                itemView.tvScpTitle.text = data as String
            }
        }
    }
}
