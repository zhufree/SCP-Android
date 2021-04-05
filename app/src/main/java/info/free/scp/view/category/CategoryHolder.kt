package info.free.scp.view.category

import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.item_category.view.*
import org.jetbrains.anko.backgroundColor

/**
 * Created by zhufree on 2018/8/22.
 *
 */

class CategoryHolder(view: View) : RecyclerView.ViewHolder(view){
    private val categoryHeight = PreferenceUtil.getCategoryHeight()
    private val categoryInterval = PreferenceUtil.getCategoryInterval()

    init {
        val rlLp = itemView.cl_category_item.layoutParams as FrameLayout.LayoutParams
        rlLp.height = Utils.dp2px(categoryHeight)
        val lp = itemView.cv_category_item.layoutParams as RecyclerView.LayoutParams
        lp.topMargin = Utils.dp2px(categoryInterval)
        lp.bottomMargin = Utils.dp2px(categoryInterval/2)
    }
    fun setData(data: Any, type: Int) {
        when (type) {
            SERIES -> {
                val start = data.toString()
                itemView.tv_scp_title.text = "SCP系列${if (start == "0") "0" else start.substring(0, start.length - 1)}1+"
            }
            SERIES_CN -> {
                val start = data.toString()
                itemView.tv_scp_title.text = "SCP-CN系列${if (start == "0") "0" else start.substring(0,start.length - 2)}01+"
            }
            else -> {
                itemView.tv_scp_title.text = data as String
            }
        }
    }

    fun refreshTheme() {
        itemView.cl_category_item.backgroundColor = Color.WHITE
        itemView.tv_scp_title.setTextColor(ThemeUtil.darkText)
    }
}
