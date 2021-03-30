package info.free.scp.view.category

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import info.free.scp.R
import info.free.scp.SCPConstants.Entry.SCP_CN_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.widget.DocGroupItem
import kotlinx.android.synthetic.main.activity_category_list.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor

class CategoryListActivity : BaseActivity() {
    private var entryType = SCP_DOC
    private var seriesList = emptyList<TextView>().toMutableList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)
        baseToolbar = category_toolbar
        category_toolbar?.inflateMenu(R.menu.category_menu) //设置右上角的填充菜单
        category_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    // TODO
                }
            }
            true
        }
        initData()
    }

    private fun getTitlePrefix(): String {
        return when (entryType) {
            SCP_DOC -> "SCP系列"
            SCP_CN_DOC -> "SCP-CN系列"
            else -> "SCP系列"
        }
    }

    private fun getSeriesTitle(index: Int): String {
        return when (index) {
            101 -> "搞笑SCP"
            102 -> "已解明SCP"
            else -> "系列$index"
        }
    }

    private fun createSeriesItem(i: Int): TextView {
        val newSeriesItem = TextView(this)
        newSeriesItem.text = getSeriesTitle(i)
        if (i == 1) {
            newSeriesItem.textColor = Color.WHITE
            newSeriesItem.background = ThemeUtil.customShape(ThemeUtil.toolbarBg,
                    0, 0, dip(18))
        } else {
            newSeriesItem.textColor = ThemeUtil.darkText
        }
        newSeriesItem.textSize = 16f
        newSeriesItem.textAlignment = View.TEXT_ALIGNMENT_CENTER
        newSeriesItem.gravity = CENTER
        newSeriesItem.onClick {
            switchSeries(i)
            seriesList.forEach {
                it.textColor = ThemeUtil.darkText
                it.background = null
            }
            newSeriesItem.textColor = Color.WHITE
            newSeriesItem.background = ThemeUtil.customShape(ThemeUtil.toolbarBg,
                    0, 0, dip(18))
        }
        seriesList.add(newSeriesItem)
        return newSeriesItem
    }

    private fun initData() {
        entryType = intent.getIntExtra("entry_type", -1)
        // 左侧列表
        val lp = LinearLayout.LayoutParams(dip(84), dip(36))
        lp.topMargin = dip(36)
        lp.leftMargin = dip(10)
        when (entryType) {
            SCP_DOC -> {
                supportActionBar?.setTitle(R.string.entry_scp_series)
                for (i in 1..6) {
                    ll_side_bar.addView(createSeriesItem(i), lp)
                }
                ll_side_bar.addView(createSeriesItem(101), lp)
                ll_side_bar.addView(createSeriesItem(102), lp)
                switchSeries(1)
            }
            SCP_CN_DOC -> {
                supportActionBar?.setTitle(R.string.entry_scp_cn_series)
                for (i in 1..3) {

                    ll_side_bar.addView(createSeriesItem(i), lp)
                }
                ll_side_bar.addView(createSeriesItem(101), lp)
                ll_side_bar.addView(createSeriesItem(102), lp)
                switchSeries(1)
            }
            else -> {

            }
        }
    }

    private val categoryCount = PreferenceUtil.getCategoryCount()
    private fun switchSeries(index: Int) {
        tv_group_title.text = getSeriesTitle(index)
        tv_group_desc.visibility = GONE
        val groupList = (0 until (1000 / categoryCount)).map { (it + (index - 1) * 10) * categoryCount }
        val lp = LinearLayout.LayoutParams(MATCH_PARENT, dip(60))
        lp.topMargin = dip(10)
        lp.leftMargin = dip(10)
        lp.rightMargin = dip(10)
        val viewCount = ll_group_list.childCount
        for (i in 2 until viewCount) {
            info { ll_group_list.getChildAt(i) }
            ll_group_list.removeAllViews()
        }
        groupList.forEach {
            val newGroupItem = DocGroupItem(this, "${getTitlePrefix()}$it+")
            newGroupItem.onClick {
                // go to doc list
            }
            ll_group_list.addView(newGroupItem, lp)
        }
    }
}