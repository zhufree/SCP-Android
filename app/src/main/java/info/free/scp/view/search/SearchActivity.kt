package info.free.scp.view.search

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants.SearchType.CONTENT
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : BaseActivity() {
    var adapter: SearchResultAdapter? = null
    var fragmentList = arrayListOf<BaseFragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initToolbar()
        val keyword = intent?.getStringExtra("keyword") ?: ""
        supportActionBar?.title = "搜索“$keyword”"
        fragmentList = arrayListOf(SearchResultFragment.newInstance(TITLE, keyword),
                SearchResultFragment.newInstance(CONTENT, keyword))
        val titleList = arrayListOf("标题搜索", "全文搜索")
        val laterPagerAdapter = TabFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_search_result?.adapter = laterPagerAdapter
        tab_search_result?.setupWithViewPager(vp_search_result)
    }

    private fun initToolbar() {
        setSupportActionBar(search_toolbar)
        search_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        search_toolbar?.setNavigationOnClickListener { finish() }
    }
}
