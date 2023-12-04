package info.free.scp.view.search

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import info.free.scp.R
import info.free.scp.SCPConstants.SearchType.CONTENT
import info.free.scp.SCPConstants.SearchType.TAG
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.databinding.ActivitySearchBinding
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager
import info.free.scp.view.tag.TagDetailActivity
import org.jetbrains.anko.startActivity

class SearchActivity : BaseActivity() {
    var adapter: SearchResultAdapter? = null
    var fragmentList = arrayListOf<BaseFragment>()
    private lateinit var binding: ActivitySearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.searchToolbar
        val keyword = intent?.getStringExtra("keyword") ?: ""
        supportActionBar?.title = "搜索“$keyword”"
        fragmentList = arrayListOf(
            SearchResultFragment.newInstance(TITLE, keyword),
            SearchResultFragment.newInstance(CONTENT, keyword),
            SearchResultFragment.newInstance(TAG, keyword)
        )
        val titleList = arrayListOf("标题搜索", "全文搜索", "Tag搜索")
        val laterPagerAdapter =
            TabFragmentPager(this, fragmentList, fragmentList.size)
        binding.vpSearchResult.adapter = laterPagerAdapter
        TabLayoutMediator(binding.tabSearchResult, binding.vpSearchResult) { tab, position ->
            tab.text = titleList[position]
        }.attach()
        binding.tabSearchResult.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (binding.tabSearchResult.selectedTabPosition == 2) {
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
