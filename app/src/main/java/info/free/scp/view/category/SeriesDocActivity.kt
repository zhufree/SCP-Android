package info.free.scp.view.category

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.Category.ABOUT_STUFF
import info.free.scp.SCPConstants.Category.JOKE
import info.free.scp.SCPConstants.Category.JOKE_CN
import info.free.scp.SCPConstants.Category.SCP_ABNORMAL
import info.free.scp.SCPConstants.Category.SCP_ARCHIVED
import info.free.scp.SCPConstants.Category.SCP_DECOMMISSIONED
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Category.SCP_REMOVED
import info.free.scp.SCPConstants.Category.SETTINGS
import info.free.scp.SCPConstants.Category.SETTINGS_CN
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.SCPConstants.Entry.ABOUT_STUFF
import info.free.scp.SCPConstants.Entry.JOKE_DOC
import info.free.scp.SCPConstants.Entry.MORE_ABOUT
import info.free.scp.SCPConstants.Entry.OTHER_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
import info.free.scp.SCPConstants.Entry.SETTINGS_DOC
import info.free.scp.SCPConstants.Entry.STORY_DOC
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.HomeFragmentPager
import kotlinx.android.synthetic.main.activity_series.*

/**
 * 一级入口，用tab分不同类别的目录
 */
class SeriesDocActivity : BaseActivity() {
    private var entryType = -1
    private var fragmentList = emptyList<BaseFragment>()
    private var titleList = emptyList<String>()
    private var currentFragment: BaseFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)
        setSupportActionBar(series_toolbar)
        series_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        series_toolbar?.setNavigationOnClickListener { finish() }
        series_toolbar?.inflateMenu(R.menu.category_menu) //设置右上角的填充菜单
        series_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    if (currentFragment is CategoryFragment) {
                        (currentFragment as CategoryFragment?)?.reverseScpList()
                    } else if (currentFragment is ScpListFragment) {
                        (currentFragment as ScpListFragment?)?.reverseScpList()
                    }
                }
            }
            true
        }
        initData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
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
            SCPConstants.Entry.ABOUT_STUFF -> {
                supportActionBar?.setTitle(R.string.title_about_documents)
                vp_series_doc.visibility = GONE
                tab_series_doc.visibility = GONE
                supportFragmentManager.beginTransaction().replace(R.id.fl_scp_item_container,
                        ScpItemFragment.newInstance(SCPConstants.Category.ABOUT_STUFF, 0)).commit()
                fl_scp_item_container.visibility = VISIBLE
            }
            JOKE_DOC -> {
                supportActionBar?.setTitle(R.string.title_joke_scp)
                fragmentList = arrayOf(ScpListFragment.newInstance(JOKE, 0),
                        ScpListFragment.newInstance(JOKE_CN, 0))
                        .toList()
                titleList = arrayOf("搞笑SCP", "搞笑SCP-CN").toList()
            }
            SETTINGS_DOC -> {
                supportActionBar?.setTitle(R.string.title_settings)
                fragmentList = arrayOf(ScpListFragment.newInstance(SETTINGS, 0),
                        ScpListFragment.newInstance(SETTINGS_CN, 0))
                        .toList()
                titleList = arrayOf("设定中心", "中国分部设定中心").toList()
            }
            MORE_ABOUT -> {
                // TODO 更多相关
                supportActionBar?.setTitle(R.string.title_more_about)
            }
        }
        if (fragmentList.isNotEmpty()) {
            currentFragment = fragmentList[0]
        }
        val scpPagerAdapter = HomeFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_series_doc?.adapter = scpPagerAdapter
        tab_series_doc?.setupWithViewPager(vp_series_doc)

        vp_series_doc?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                currentFragment = fragmentList[p0]
            }
        })
    }
}
