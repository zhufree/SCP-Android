package info.free.scp.view.category

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.SCPConstants.Category.JOKE
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Entry.SCP_CN_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.bean.ScpModel
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.widget.DocGroupItem
import kotlinx.android.synthetic.main.activity_category_list.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor

/**
 * 2-3层
 */
class GroupListActivity : BaseActivity() {
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
            JOKE -> "搞笑SCP"
            SCP_EX -> "已解明SCP"
            else -> "系列$index"
        }
    }

    private fun getSaveType(type: Int): Int {
        return when (entryType) {
            SCP_DOC -> {
                when (type) {
                    JOKE -> SAVE_JOKE
                    SCP_EX -> SAVE_EX
                    else -> SAVE_SERIES
                }
            }
            SCP_CN_DOC -> {
                when (type) {
                    JOKE -> SAVE_JOKE_CN
                    SCP_EX -> SAVE_EX_CN
                    else -> SAVE_SERIES_CN
                }
            }
            else -> SAVE_SERIES
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
            if (i < 100) {
                newSeriesItem.textColor = Color.WHITE
                newSeriesItem.background = ThemeUtil.customShape(ThemeUtil.toolbarBg,
                        0, 0, dip(18))
            }
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

    private fun getGroupListTitle(index: Int): List<String> {
        var groupList = listOf<String>()
        groupList = when (index) {
            else -> {
                (0 until (1000 / categoryCount)).map { "${getTitlePrefix()}${(it + (index - 1) * 10) * categoryCount}+" }
            }
        }
        return groupList
    }

    private val categoryCount = PreferenceUtil.getCategoryCount()

    private fun switchSeries(index: Int) {
        tv_group_desc.visibility = GONE

        if (index < 100) {
            tv_group_title.text = getSeriesTitle(index)

            val lp = LinearLayout.LayoutParams(MATCH_PARENT, dip(60))
            lp.topMargin = dip(10)
            lp.leftMargin = dip(10)
            lp.rightMargin = dip(10)

            ll_group_list.removeAllViews()
            getGroupListTitle(index).forEachIndexed { i, s ->
                val newGroupItem = DocGroupItem(this, s)
                newGroupItem.onClick {
                    startActivity<DocListActivity>("saveType" to getSaveType(index),
                            "groupIndex" to (index - 1) * 10 + i
                    )
                }
                ll_group_list.addView(newGroupItem, lp)
            }
        } else {
            // 直接跳列表页
            startActivity<DocListActivity>("saveType" to getSaveType(index))
        }
    }
}