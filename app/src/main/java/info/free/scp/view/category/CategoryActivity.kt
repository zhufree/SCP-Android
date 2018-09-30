package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import info.free.scp.R
import info.free.scp.SCPConstants.CONTEST
import info.free.scp.SCPConstants.CONTEST_CN
import info.free.scp.SCPConstants.EVENT
import info.free.scp.SCPConstants.SAVE_CONTEST
import info.free.scp.SCPConstants.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.SAVE_SERIES
import info.free.scp.SCPConstants.SAVE_ARCHIVED
import info.free.scp.SCPConstants.SAVE_SERIES_CN
import info.free.scp.SCPConstants.SAVE_DECOMMISSIONED
import info.free.scp.SCPConstants.SAVE_EX
import info.free.scp.SCPConstants.SAVE_JOKE
import info.free.scp.SCPConstants.SAVE_JOKE_CN
import info.free.scp.SCPConstants.SAVE_REMOVED
import info.free.scp.SCPConstants.SAVE_SERIES_STORY_1
import info.free.scp.SCPConstants.SAVE_SERIES_STORY_2
import info.free.scp.SCPConstants.SAVE_SERIES_STORY_3
import info.free.scp.SCPConstants.SAVE_SETTINGS
import info.free.scp.SCPConstants.SAVE_SETTINGS_CN
import info.free.scp.SCPConstants.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.SAVE_TALES_CN_PREFIX
import info.free.scp.SCPConstants.SAVE_TALES_PREFIX
import info.free.scp.SCPConstants.SETTINGS
import info.free.scp.SCPConstants.SETTINGS_CN
import info.free.scp.SCPConstants.STORY_SERIES
import info.free.scp.SCPConstants.STORY_SERIES_CN
import info.free.scp.SCPConstants.TALES
import info.free.scp.SCPConstants.TALES_BY_TIME
import info.free.scp.SCPConstants.TALES_CN
import info.free.scp.SCPConstants.SERIES
import info.free.scp.SCPConstants.SERIES_ABOUT
import info.free.scp.SCPConstants.SERIES_ARCHIVED
import info.free.scp.SCPConstants.SERIES_CN
import info.free.scp.SCPConstants.SERIES_STORY
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import info.free.scp.view.WebActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : BaseActivity() {
    private val categoryList: MutableList<Any> = emptyList<Any>().toMutableList()
    private val scpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var categoryAdapter: CategoryAdapter? = null
    private var scpAdapter: ScpAdapter? = null
    private var pageType = 0 //一级目录和二级目录
    private var categoryType = -1
    private var isCnPage = false // 是否是cn页面 归档内容部分用到
    private var onlyOneLayer = false
    private val tag = "category"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            if (pageType == 0 || onlyOneLayer) {
                finish()
            } else {
                pageType = 0
                initData()
            }
        }

        val lm = LinearLayoutManager(this, VERTICAL, false)
        rlScpList.layoutManager = lm

        initData()

        toolbar.inflateMenu(R.menu.series_category_menu) //设置右上角的填充菜单
        toolbar.setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.reverse -> {
                    when (pageType) {
                        0 -> {
                            categoryList.reverse()
                            categoryAdapter?.notifyDataSetChanged()
                        }
                        1-> {
                            scpList.reverse()
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                R.id.cn_page -> {
                    isCnPage = !isCnPage
                    initData()
                }
            }
            true
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i(tag, "pageType = $pageType")
            if (pageType == 0 || onlyOneLayer) {
                finish()
            } else {
                Log.i(tag, "回到上级目录，初始化数据")
                pageType = 0
                initData()
            }
        }
        return false
    }

    private fun initScpAdapter() {
        if (scpAdapter == null) {
            Log.i(tag, "初始化scpAdapter")
            scpAdapter = ScpAdapter(this@CategoryActivity, scpList)
            scpAdapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val intent = Intent()
                    intent.putExtra("link", scpList[position].link)
                    intent.setClass(this@CategoryActivity, WebActivity::class.java)
                    startActivity(intent)
                }
            }
            rlScpList.adapter = scpAdapter
            scpAdapter?.notifyDataSetChanged()
        } else {
            rlScpList.adapter = scpAdapter
            scpAdapter?.notifyDataSetChanged()
        }
    }

    private fun initData() {
        categoryType  = intent.getIntExtra("saveType", -1)
        Log.i(tag, "categoryType = $categoryType, pageType = $pageType")
        categoryList.clear()
        scpList.clear()
        // 一级目录
        when (categoryType) {
            SERIES -> {
                pageType = 0
                categoryList.addAll(arrayOf(0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500))
            }
            SERIES_CN -> {
                pageType = 0
                categoryList.addAll(arrayOf(0, 100, 200, 300, 400, 500, 600, 700, 800, 900))
            }
            SERIES_STORY -> {
                pageType = 0
                categoryList.addAll(arrayOf("SCP系列1故事版1~500","SCP系列1故事版501~1000","SCP系列1故事版1001~1500",
                        "SCP系列1故事版1501~1808", "SCP系列2故事版1~500", "SCP系列2故事版501~1000", "SCP系列2故事版1001~1193",
                        "SCP系列3故事版1~500","SCP系列3故事版501~1000","SCP系列3故事版1000~1211"))
            }
            SERIES_ARCHIVED -> {
                pageType = 0
                categoryList.addAll(arrayOf("搞笑SCP", "异常物品记录", "超常事件记录"))
                categoryList.addAll(if (isCnPage) arrayOf("已解明SCP") else arrayOf("前SCP", "被归档的SCP", "废除SCP", "删除SCP"))
            }
            TALES -> {
                // 1021
                pageType = 0
                categoryList.addAll(arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y","Z", "0-9"))
            }
            TALES_CN -> {
                // 1021
                pageType = 0
                categoryList.addAll(arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y","Z", "0-9"))
            }
            SERIES_ABOUT, STORY_SERIES, STORY_SERIES_CN, SETTINGS, SETTINGS_CN,
            CONTEST, CONTEST_CN, EVENT, TALES_BY_TIME -> {
                pageType = 1
                onlyOneLayer = true
            }
        }

        if (pageType == 0) {
            if (categoryAdapter == null) {
                categoryAdapter = CategoryAdapter(this, categoryType, categoryList)
                rlScpList.adapter = categoryAdapter
                categoryAdapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        Log.i(tag, "onItemClick")
                        pageType = 1
                        initScpAdapter()
                        getScpList(position)
                    }
                }
            } else {
                categoryAdapter?.notifyDataSetChanged()
                rlScpList.adapter = categoryAdapter
            }
        } else {
            getScpList(0)
            initScpAdapter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.series_category_menu, menu)
        return true
    }

    private fun getScpList(position: Int) {
        Log.i("category", "加载scp列表")
        scpList.clear()
        when (categoryType) {
            SERIES -> {
                // 0,499,999
                val start = if (position == 0) 0 else position*500
                val limit = 500
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES, start, limit))
            }
            SERIES_CN -> {
                val start = if (position == 0) 0 else position*100 - 1
                val limit = if (start == 0) 99 else 100
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_CN, start, limit))

            }
            SERIES_STORY -> {
                when (position) {
                    // 故事版1~499
                    0 -> {
                        val start = 0
                        val limit = 499
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_1, start, limit))
                    }
                    // 故事版1 500~999
                    1 -> {
                        val start = 499
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_1, start, limit))
                    }
                    // 故事版1 1000~1499
                    2 -> {
                        val start = 999
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_1, start, limit))
                    }
                    // 故事版1 1500~1808
                    3 -> {
                        val start = 1499
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_1, start, limit))
                    }
                    // 故事版2 1~499
                    4 -> {
                        val start = 0
                        val limit = 499
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_2, start, limit))
                    }
                    // 故事版2 500~999
                    5 -> {
                        val start = 499
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_2, start, limit))
                    }
                    // 故事版2 1000~1193
                    6 -> {
                        val start = 999
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_2, start, limit))
                    }
                    // 故事版3 1~499
                    7 -> {
                        val start = 0
                        val limit = 499
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_3, start, limit))
                    }
                    // 故事版3 500~999
                    8 -> {
                        val start = 499
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_3, start, limit))
                    }
                    // 故事版3 1000~1211
                    9 -> {
                        val start = 999
                        val limit = 500
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_3, start, limit))
                    }
                }
            }
            SERIES_ABOUT -> {
                if (PreferenceUtil.getInitAboutData()) {
                    scpList.addAll(ScpDao.getInstance().getBasicInfo())
                } else {
                    ScpDao.getInstance().initBasicInfo()
                    scpList.addAll(ScpDao.getInstance().getBasicInfo())
                    PreferenceUtil.setInitAboutData()
                }
            }
            SERIES_ARCHIVED -> {
                // 内容较少，直接全部加载
                when (position) {
                    0 -> {
                        // 搞笑scp
                        if (isCnPage) {
                            // cn
                            scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_JOKE_CN))

                        } else {
                            scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_JOKE))
                        }
                    }
                    3 -> {
                        // 已解明scp
                        scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_EX))
                    }
                    4 -> {
                        // 归档scp
                        scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_ARCHIVED))
                    }
                    5 -> {
                        // 废弃scp
                        scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_DECOMMISSIONED))
                    }
                    6 -> {
                        // 已移除scp
                        scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_REMOVED))
                    }
                    else -> {
                        Toaster.show("开发中...")
                    }
                }
            }
            TALES -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_TALES_PREFIX + categoryList[position]))
            }
            TALES_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_TALES_CN_PREFIX + categoryList[position]))
            }
            STORY_SERIES -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_STORY_SERIES))
            }
            STORY_SERIES_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_STORY_SERIES_CN))
            }
            SETTINGS -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_SETTINGS))
            }
            SETTINGS_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_SETTINGS_CN))
            }
            CONTEST -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_CONTEST))
            }
            CONTEST_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_CONTEST_CN))
            }
            EVENT -> { Toaster.show("开发中...") }
            TALES_BY_TIME -> { Toaster.show("开发中...") }
        }
        if (scpList.size == 0) {
            Toaster.show("该页没有内容或数据初始化未完成，请稍等")
        }
        scpAdapter?.notifyDataSetChanged()
    }
}
