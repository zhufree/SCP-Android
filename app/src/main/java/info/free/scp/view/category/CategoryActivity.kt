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
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.Category.CONTEST
import info.free.scp.SCPConstants.Category.CONTEST_CN
import info.free.scp.SCPConstants.Category.EVENT
import info.free.scp.SCPConstants.SaveType.SAVE_ABOUT
import info.free.scp.SCPConstants.SaveType.SAVE_CONTEST
import info.free.scp.SCPConstants.SaveType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES
import info.free.scp.SCPConstants.SaveType.SAVE_ARCHIVED
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.SaveType.SAVE_DECOMMISSIONED
import info.free.scp.SCPConstants.SaveType.SAVE_EVENT
import info.free.scp.SCPConstants.SaveType.SAVE_EX
import info.free.scp.SCPConstants.SaveType.SAVE_JOKE
import info.free.scp.SCPConstants.SaveType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.SaveType.SAVE_REMOVED
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_STORY_1
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_STORY_2
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_STORY_3
import info.free.scp.SCPConstants.SaveType.SAVE_SETTINGS
import info.free.scp.SCPConstants.SaveType.SAVE_SETTINGS_CN
import info.free.scp.SCPConstants.SaveType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.SaveType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.SaveType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.SaveType.SAVE_TALES_CN_PREFIX
import info.free.scp.SCPConstants.SaveType.SAVE_TALES_PREFIX
import info.free.scp.SCPConstants.Category.SETTINGS
import info.free.scp.SCPConstants.Category.SETTINGS_CN
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_BY_TIME
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_ABOUT
import info.free.scp.SCPConstants.Category.SERIES_ARCHIVED
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.SCPConstants.Category.SERIES_STORY
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_category.*
import kotlin.math.max
import kotlin.math.min

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
    private var currentScpPosition = -1
    private var currentCategoryPosition = -1

    private val eventScpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private val taleTimeList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(category_toolbar)
        category_toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        category_toolbar.setNavigationOnClickListener {
            if (pageType == 0 || onlyOneLayer) {
                finish()
            } else {
                pageType = 0
                initData()
            }
        }

        val lm = LinearLayoutManager(this, VERTICAL, false)
        rv_scp_list.layoutManager = lm

        initData()

        category_toolbar.inflateMenu(R.menu.category_menu) //设置右上角的填充菜单
        category_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.reverse -> {
                    when (pageType) {
                        0 -> {
                            categoryList.reverse()
                            categoryAdapter?.notifyDataSetChanged()
                        }
                        1 -> {
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

    override fun onResume() {
        super.onResume()
        if (pageType == 1 && currentCategoryPosition > -1 && currentCategoryPosition < categoryList.size) {
            getScpList(currentCategoryPosition)
            rv_scp_list?.scrollToPosition(currentScpPosition)
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
                    currentScpPosition = position
                    EventUtil.onEvent(this@CategoryActivity, EventUtil.clickDetail, scpList[position].link)
                    PreferenceUtil.addPoints(2)
                    val intent = Intent()
                    intent.putExtra("link", scpList[position].link)
                    intent.putExtra("sId", scpList[position].sId)
                    intent.setClass(this@CategoryActivity, DetailActivity::class.java)
                    startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)
                }
            }
            rv_scp_list.adapter = scpAdapter
            scpAdapter?.notifyDataSetChanged()
        } else {
            rv_scp_list.adapter = scpAdapter
            scpAdapter?.notifyDataSetChanged()
        }
    }

    private fun initData() {
        categoryType = intent.getIntExtra("saveType", -1)
        Log.i(tag, "categoryType = $categoryType, pageType = $pageType")
        categoryList.clear()
        scpList.clear()
        // 一级目录
        when (categoryType) {
            SERIES -> {
                pageType = 0
                categoryList.addAll((0 until 25).map { it*200 })
            }
            SERIES_CN -> {
                pageType = 0
                categoryList.addAll((0 until 9).map { it*100 })
            }
            SERIES_STORY -> {
                pageType = 0
                categoryList.addAll(arrayOf("SCP系列1故事版1~500", "SCP系列1故事版501~1000", "SCP系列1故事版1001~1500",
                        "SCP系列1故事版1501~1808", "SCP系列2故事版1~500", "SCP系列2故事版501~1000", "SCP系列2故事版1001~1193",
                        "SCP系列3故事版1~500", "SCP系列3故事版501~1000", "SCP系列3故事版1000~1211"))
                categoryList.addAll((0 until 9).map { "SCP系列1故事版${it*200 + 1}~${min(it*200+200, 1808)}" })
                categoryList.addAll((0 until 6).map { "SCP系列2故事版${it*200 + 1}~${min(it*200+200, 1193)}" })
                categoryList.addAll((0 until 7).map { "SCP系列3故事版${it*200 + 1}~${min(it*200+200, 1211)}" })
            }
            SERIES_ARCHIVED -> {
                pageType = 0
                categoryList.addAll(arrayOf("搞笑SCP"))
                categoryList.addAll(if (isCnPage) arrayOf("已解明SCP") else arrayOf("前SCP", "被归档的SCP", "废除SCP", "删除SCP"))
            }
            TALES -> {
                // 1021
                pageType = 0
                categoryList.addAll(arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9"))
            }
            TALES_CN -> {
                // 1021
                pageType = 0
                categoryList.addAll(arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9"))
            }
            EVENT -> {
                categoryList.addAll(arrayOf("实验记录", "探索报告", "事故/事件报告", "访谈记录", "独立补充材料"))
            }
            TALES_BY_TIME -> {
                categoryList.addAll(arrayOf("2014", "2015", "2016", "2017", "2018"))
            }
            SERIES_ABOUT, STORY_SERIES, STORY_SERIES_CN, SETTINGS, SETTINGS_CN,
            CONTEST, CONTEST_CN -> {
                pageType = 1
                onlyOneLayer = true
            }
        }

        if (pageType == 0) {
            if (categoryAdapter == null) {
                categoryAdapter = CategoryAdapter(this, categoryType, categoryList)
                rv_scp_list.adapter = categoryAdapter
                categoryAdapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        Log.i(tag, "onItemClick")
                        pageType = 1
                        currentCategoryPosition = position
                        initScpAdapter()
                        getScpList(position)
                    }
                }
            } else {
                categoryAdapter?.notifyDataSetChanged()
                rv_scp_list.adapter = categoryAdapter
                if (currentCategoryPosition > 0) {
                    rv_scp_list?.scrollToPosition(currentCategoryPosition)
                }
            }
        } else {
            getScpList(0)
            initScpAdapter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.category_menu, menu)
        return true
    }

    private fun getScpList(position: Int) {
        Log.i("category", "加载scp列表")
        scpList.clear()
        when (categoryType) {
            SERIES -> {
                // 0,499,999
                val start = if (position == 0) 0 else position * 200
                val limit = 200
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES, start, limit))
            }
            SERIES_CN -> {
                val start = if (position == 0) 0 else position * 100 - 1
                val limit = if (start == 0) 99 else 100
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_CN, start, limit))

            }
            SERIES_STORY -> {
                when (position) {
                    // 故事版1~499
                    in 0 until 9 -> {
                        val start = if (position == 0) 0 else position * 200
                        val limit = 200
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_1, start, limit))
                    }
                    // 故事版2 1~499
                    in 9 until 15 -> {
                        val subPosition = position - 9
                        val start = if (subPosition == 0) 0 else subPosition * 200
                        val limit = 200
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_2, start, limit))
                    }
                    // 故事版3 1~499
                    in 15 until 22 -> {
                        val subPosition = position - 15
                        val start = if (subPosition == 0) 0 else subPosition * 200
                        val limit = 200
                        scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SAVE_SERIES_STORY_3, start, limit))
                    }
                }
            }
            SERIES_ABOUT -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SAVE_ABOUT))
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
            EVENT -> {
                if (eventScpList.isEmpty()) {
                    eventScpList.addAll(ScpDao.getInstance().getScpByType(SAVE_EVENT))
                }
                when (position) {
                    0 -> {
                        scpList.addAll(eventScpList.filter { it.evenType == "lab_record" })
                    }
                    1 -> {
                        scpList.addAll(eventScpList.filter { it.evenType == "discovery_report" })
                    }
                    2 -> {
                        scpList.addAll(eventScpList.filter { it.evenType == "event_report" })
                    }
                    3 -> {
                        scpList.addAll(eventScpList.filter { it.evenType == "interview" })
                    }
                    4 -> {
                        scpList.addAll(eventScpList.filter { it.evenType == "addon" })
                    }
                }
            }
            TALES_BY_TIME -> {
                if (taleTimeList.isEmpty()) {
                    taleTimeList.addAll(ScpDao.getInstance().getScpByType(SAVE_TALES_BY_TIME))
                }
                when (position) {
                    0 -> {
                        scpList.addAll(taleTimeList.filter { it.month.startsWith("2018") })
                    }
                    1 -> {
                        scpList.addAll(taleTimeList.filter { it.month.startsWith("2017") })
                    }
                    2 -> {
                        scpList.addAll(taleTimeList.filter { it.month.startsWith("2016") })
                    }
                    3 -> {
                        scpList.addAll(taleTimeList.filter { it.month.startsWith("2015") })
                    }
                    4 -> {
                        scpList.addAll(taleTimeList.filter { it.month.startsWith("2014") })
                    }
                }

            }
        }
        if (scpList.size == 0) {
            Toaster.show("该页没有内容或数据初始化未完成")
        }
        scpAdapter?.notifyDataSetChanged()
    }
}
