package info.free.scp.view.home

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_series.*

class SeriesActivity : BaseActivity() {
    private val seriesFragment = CategoryFragment.newInstance(SERIES)
    private val seriesCnFragment = CategoryFragment.newInstance(SERIES_CN)
    // 一级目录
//    private val exScpFragment = LibraryFragment.newInstance()
    private var fragmentList = arrayOf(seriesFragment, seriesCnFragment).toList()
    private var titleList = arrayOf("SCP系列", "SCP-CN系列").toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)
        setSupportActionBar(series_toolbar)
        series_toolbar?.setTitle(R.string.title_scp_documents)
        series_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        series_toolbar?.setNavigationOnClickListener { finish() }
        val scpPagerAdapter = HomeFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_series_doc?.adapter = scpPagerAdapter
        tab_series_doc?.setupWithViewPager(vp_series_doc)
    }
}
