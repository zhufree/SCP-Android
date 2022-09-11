package info.free.scp.view.search

import android.os.Bundle
import android.util.Log
import com.google.android.material.tabs.TabLayout
import info.free.scp.R
import info.free.scp.SCPConstants.SearchType.CONTENT
import info.free.scp.SCPConstants.SearchType.TAG
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager
import info.free.scp.view.tag.TagDetailActivity
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.startActivity

class SearchActivity : BaseActivity() {
    var adapter: SearchResultAdapter? = null
    var fragmentList = arrayListOf<BaseFragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        baseToolbar = search_toolbar
        val keyword = intent?.getStringExtra("keyword") ?: ""
        supportActionBar?.title = "搜索“$keyword”"
        fragmentList = arrayListOf(
            SearchResultFragment.newInstance(TITLE, keyword),
            SearchResultFragment.newInstance(CONTENT, keyword),
            SearchResultFragment.newInstance(TAG, keyword)
        )
        val titleList = arrayListOf("标题搜索", "全文搜索", "Tag搜索")
        val laterPagerAdapter =
            TabFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_search_result?.adapter = laterPagerAdapter
        tab_search_result?.setupWithViewPager(vp_search_result)
        tab_search_result?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab_search_result.selectedTabPosition == 2) {
                    startActivity<TagDetailActivity>("tag" to keyword)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
}
