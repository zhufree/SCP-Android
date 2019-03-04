package info.free.scp.view.category

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.view.Menu
import info.free.scp.R
import info.free.scp.SCPConstants.Category.ABOUT_INFO
import info.free.scp.SCPConstants.Category.ABOUT_INTRO
import info.free.scp.SCPConstants.Category.CONTEST
import info.free.scp.SCPConstants.Category.CONTEST_CN
import info.free.scp.SCPConstants.Category.JOKE
import info.free.scp.SCPConstants.Category.JOKE_CN
import info.free.scp.SCPConstants.Category.SCP_ABNORMAL
import info.free.scp.SCPConstants.Category.SCP_ARCHIVES
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.SCPConstants.Category.SCP_EX
import info.free.scp.SCPConstants.Category.SETTINGS
import info.free.scp.SCPConstants.Category.SETTINGS_CN
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.SCPConstants.Entry.ABOUT_SCP_DOC
import info.free.scp.SCPConstants.Entry.JOKE_DOC
import info.free.scp.SCPConstants.Entry.SCP_DOC
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
                fragmentList = arrayOf(
                        CategoryFragment.newInstance(SERIES, 0),
                        CategoryFragment.newInstance(SERIES, 1),
                        CategoryFragment.newInstance(SERIES, 2),
                        CategoryFragment.newInstance(SERIES, 3),
                        CategoryFragment.newInstance(SERIES, 4),
                        CategoryFragment.newInstance(SERIES_CN, 0),
                        CategoryFragment.newInstance(SERIES_CN, 1),
                        ScpListFragment.newInstance(SCP_EX, 0), // cn合并
                        CategoryFragment.newInstance(SCP_ARCHIVES, 0)
                )
                        .toList()
                titleList = arrayOf("SCP系列1", "SCP系列2", "SCP系列3", "SCP系列4", "SCP系列5",
                        "SCP-CN系列1", "SCP-CN系列2", "已解明SCP", "其他SCP").toList()
            }
            STORY_DOC -> {
                supportActionBar?.setTitle(R.string.title_scp_story)
                fragmentList = arrayOf(CategoryFragment.newInstance(TALES, 0),
                        CategoryFragment.newInstance(TALES_CN, 0),
                        ScpListFragment.newInstance(STORY_SERIES, 0),
                        ScpListFragment.newInstance(STORY_SERIES_CN, 0),
                        ScpListFragment.newInstance(SCP_ABNORMAL, 0)
                )
                        .toList()
                titleList = arrayOf("基金会故事", "中国分部原创故事", "故事系列", "中国分部故事系列", "异常物品+超常记录+三句话外围").toList()
            }
            ABOUT_SCP_DOC -> {
                supportActionBar?.setTitle(R.string.title_about_scp)
                fragmentList = arrayOf(
                        ScpListFragment.newInstance(SETTINGS, 0),
                        ScpListFragment.newInstance(SETTINGS_CN, 0),
                        ScpListFragment.newInstance(CONTEST, 0),
                        ScpListFragment.newInstance(CONTEST_CN, 0),
                        ScpListFragment.newInstance(ABOUT_INFO, 0),
                        ScpListFragment.newInstance(ABOUT_INTRO, 0))
                        .toList()
                titleList = arrayOf("平行世界观（设定中心）", "中国分部设定中心", "征文竞赛", "中国分部征文竞赛",
                        "基金会设定", "入门简介").toList()
            }
            JOKE_DOC -> {
                supportActionBar?.setTitle(R.string.title_joke_scp)
                fragmentList = arrayOf(ScpListFragment.newInstance(JOKE, 0),
                        ScpListFragment.newInstance(JOKE_CN, 0))
                        .toList()
                titleList = arrayOf("搞笑SCP", "搞笑SCP-CN").toList()
            }
        }
        if (fragmentList.isNotEmpty()) {
            currentFragment = fragmentList[0]
        }
        val scpPagerAdapter = HomeFragmentPager(supportFragmentManager, fragmentList, titleList)
        vp_series_doc?.adapter = scpPagerAdapter
        tab_series_doc?.setupWithViewPager(vp_series_doc)
        tab_series_doc?.tabMode

        vp_series_doc?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
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
