package info.free.scp.view.home

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants.Category.SCP_ARCHIVED
import info.free.scp.SCPConstants.Category.SCP_DECOMMISSIONED
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Category.SCP_REMOVED
import info.free.scp.SCPConstants.Entry.OTHER_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.activity_series.*

/**
 * SCP文档，包括scp系列，scp-cn系列，已解明scp
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
                        CategoryFragment.newInstance(SERIES_CN), ScpListFragment.newInstance(SCP_EX))
                        .toList()
                titleList = arrayOf("SCP系列", "SCP-CN系列", "已解明SCP").toList()
            }
            OTHER_DOC -> {
                supportActionBar?.setTitle(R.string.title_other_documents)
                fragmentList = arrayOf(ScpListFragment.newInstance(SCP_ARCHIVED),
                        ScpListFragment.newInstance(SCP_DECOMMISSIONED),
                        ScpListFragment.newInstance(SCP_REMOVED))
                        .toList()
                titleList = arrayOf("归档SCP", "废除SCP", "删除SCP", "异常物品+超常记录").toList()
            }

        }
        val scpPagerAdapter = HomeFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_series_doc?.adapter = scpPagerAdapter
        tab_series_doc?.setupWithViewPager(vp_series_doc)
    }
}
