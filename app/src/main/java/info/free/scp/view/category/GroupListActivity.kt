package info.free.scp.view.category

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import info.free.scp.R
import info.free.scp.SCPConstants.Category.CONTEST
import info.free.scp.SCPConstants.Category.CONTEST_CN
import info.free.scp.SCPConstants.Category.JOKE
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Category.SETTINGS
import info.free.scp.SCPConstants.Category.SETTINGS_CN
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_BY_TIME
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.SCPConstants.Category.WANDER
import info.free.scp.SCPConstants.Category.WANDER_CN
import info.free.scp.SCPConstants.Entry.SCP_CN_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.SCPConstants.Entry.STORY_DOC
import info.free.scp.SCPConstants.Entry.WANDER_DOC
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_CANON
import info.free.scp.SCPConstants.ScpType.SAVE_CANON_CN
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_TALES
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER_CN
import info.free.scp.databinding.ActivityGroupListBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.widget.DocGroupItem
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 2-3层
 */
class GroupListActivity : BaseActivity() {
    private var entryType = SCP_DOC
    private var seriesList = emptyList<TextView>().toMutableList()
    private lateinit var binding: ActivityGroupListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.categoryToolbar
        initData()
    }

    private fun getTitlePrefix(): String {
        return when (entryType) {
            SCP_DOC -> "SCP系列"
            SCP_CN_DOC -> "SCP-CN系列"
            STORY_DOC -> "故事"
            WANDER_DOC -> "放逐者图书馆"
            else -> "SCP系列"
        }
    }

    private fun getSeriesTitle(index: Int): String {
        return when (index) {
            JOKE -> "搞笑SCP"
            SCP_EX -> "已解明SCP"
            TALES -> "基金会故事"
            TALES_CN -> "CN原创故事"
            TALES_BY_TIME -> "CN原创故事(按发布时间排列)"
            SETTINGS -> "设定中心"
            SETTINGS_CN -> "CN设定中心"
            STORY_SERIES -> "故事系列"
            STORY_SERIES_CN -> "CN故事系列"
            CONTEST -> "征文竞赛"
            CONTEST_CN -> "CN征文竞赛"
            WANDER -> "放逐者图书馆"
            WANDER_CN -> "CN放逐者图书馆"
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
            STORY_DOC -> {
                when (type) {
                    TALES -> SAVE_TALES
                    TALES_CN -> SAVE_TALES_CN
                    TALES_BY_TIME -> SAVE_TALES_BY_TIME
                    SETTINGS -> SAVE_CANON
                    SETTINGS_CN -> SAVE_CANON_CN
                    STORY_SERIES -> SAVE_STORY_SERIES
                    STORY_SERIES_CN -> SAVE_STORY_SERIES_CN
                    CONTEST -> SAVE_CONTEST
                    CONTEST_CN -> SAVE_CONTEST_CN
                    else -> SAVE_TALES
                }
            }
            WANDER_DOC -> {
                when (type) {
                    WANDER -> SAVE_WANDER
                    WANDER_CN -> SAVE_WANDER_CN
                    else -> SAVE_WANDER
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
        newSeriesItem.padding = 8
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
        val lp = LinearLayout.LayoutParams(dip(84), WRAP_CONTENT)
        lp.topMargin = dip(36)
        lp.leftMargin = dip(10)
        // 添加侧边栏
        supportActionBar?.title = getTitlePrefix()
        when (entryType) {
            SCP_DOC -> {
                for (i in 1..8) {
                    binding.llSideBar.addView(createSeriesItem(i), lp)
                }
                binding.llSideBar.addView(createSeriesItem(JOKE), lp)
                binding.llSideBar.addView(createSeriesItem(SCP_EX), lp)
                switchSeries(1)
            }
            SCP_CN_DOC -> {
                for (i in 1..3) {
                    binding.llSideBar.addView(createSeriesItem(i), lp)
                }
                binding.llSideBar.addView(createSeriesItem(JOKE), lp)
                binding.llSideBar.addView(createSeriesItem(SCP_EX), lp)
                switchSeries(1)
            }
            STORY_DOC -> {
                binding.llSideBar.addView(createSeriesItem(TALES), lp)
                binding.llSideBar.addView(createSeriesItem(TALES_CN), lp)
                binding.llSideBar.addView(createSeriesItem(TALES_BY_TIME), lp)
                binding.llSideBar.addView(createSeriesItem(SETTINGS), lp)
                binding.llSideBar.addView(createSeriesItem(SETTINGS_CN), lp)
                binding.llSideBar.addView(createSeriesItem(STORY_SERIES), lp)
                binding.llSideBar.addView(createSeriesItem(STORY_SERIES_CN), lp)
                binding.llSideBar.addView(createSeriesItem(CONTEST), lp)
                binding.llSideBar.addView(createSeriesItem(CONTEST_CN), lp)
                switchSeries(TALES)
            }
            WANDER_DOC -> {
                binding.llSideBar.addView(createSeriesItem(WANDER), lp)
                binding.llSideBar.addView(createSeriesItem(WANDER_CN), lp)
                switchSeries(WANDER)
            }
            else -> {

            }
        }
    }

    private fun getGroupListTitle(index: Int): List<String> {
        return when (index) {
            TALES, TALES_CN, WANDER, WANDER_CN -> {
                listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
                        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9")
            }
            TALES_BY_TIME -> {
                listOf("2014", "2015", "2016", "2017", "2018")
            }
            else -> {
                (0 until (1000 / categoryCount)).map { "${getTitlePrefix()}${(it + (index - 1) * 10) * categoryCount}+" }
            }
        }
    }

    private val categoryCount = 100

    private fun switchSeries(index: Int) {
        binding.tvGroupDesc.visibility = GONE

        if (index < 100) {
            binding.tvGroupTitle.text = getSeriesTitle(index)

            val lp = LinearLayout.LayoutParams(MATCH_PARENT, dip(60))
            lp.topMargin = dip(10)
            lp.leftMargin = dip(10)
            lp.rightMargin = dip(10)

            binding.llGroupList.removeAllViews()
            getGroupListTitle(index).forEachIndexed { i, s ->
                // 添加group
                val newGroupItem = DocGroupItem(this, s)
                newGroupItem.onClick {
                    startActivity<DocListActivity>(
                        "saveType" to getSaveType(index),
                        "groupIndex" to (index - 1) * 10 + i,
                        "extraType" to s
                    )
                }
                binding.llGroupList.addView(newGroupItem, lp)
            }
        } else {
            // 直接跳列表页
            startActivity<DocListActivity>("saveType" to getSaveType(index))
        }
    }
}