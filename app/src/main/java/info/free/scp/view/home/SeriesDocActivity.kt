package info.free.scp.view.home

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants.Category.SCP_ABNORMAL
import info.free.scp.SCPConstants.Category.SCP_ARCHIVED
import info.free.scp.SCPConstants.Category.SCP_DECOMMISSIONED
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Category.SCP_REMOVED
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.SCPConstants.Entry.OTHER_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.SCPConstants.Entry.STORY_DOC
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.activity_series.*

/**
 * 一级入口，用tab分不同类别的目录
 */
class SeriesDocActivity : BaseActivity() {
    private var entryType = -1
    private var fragmentList = emptyList<BaseFragment>()
    private var titleList = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)
        setSupportActionBar(series_toolbar)
        series_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        series_toolbar?.setNavigationOnClickListener { finish() }
        initData()
    }

    private fun initData() {
        entryType = intent.getIntExtra("entry_type", -1)
        when (entryType) {
            SCP_DOC -> {
                supportActionBar?.setTitle(R.string.title_scp_documents)
                fragmentList = arrayOf(CategoryFragment.newInstance(SERIES),
                        CategoryFragment.newInstance(SERIES_CN),
                        ScpListFragment.newInstance(SCP_EX, 0))
                        .toList()
                titleList = arrayOf("SCP系列", "SCP-CN系列", "已解明SCP").toList()
            }
            OTHER_DOC -> {
                supportActionBar?.setTitle(R.string.title_other_documents)
                fragmentList = arrayOf(ScpListFragment.newInstance(SCP_ARCHIVED, 0),
                        ScpListFragment.newInstance(SCP_DECOMMISSIONED, 0),
                        ScpListFragment.newInstance(SCP_REMOVED, 0),
                        ScpListFragment.newInstance(SCP_ABNORMAL, 0))
                        .toList()
                titleList = arrayOf("归档SCP", "废除SCP", "删除SCP", "异常物品+超常记录").toList()
            }
            STORY_DOC -> {
                supportActionBar?.setTitle(R.string.title_scp_story)
                fragmentList = arrayOf(CategoryFragment.newInstance(TALES),
                        CategoryFragment.newInstance(TALES_CN),
                        ScpListFragment.newInstance(STORY_SERIES, 0),
                        ScpListFragment.newInstance(STORY_SERIES_CN, 0))
                        .toList()
                titleList = arrayOf("基金会故事", "中国分部原创故事", "故事系列", "中国分部故事系列").toList()
            }

        }
        val scpPagerAdapter = HomeFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_series_doc?.adapter = scpPagerAdapter
        tab_series_doc?.setupWithViewPager(vp_series_doc)
    }
}
