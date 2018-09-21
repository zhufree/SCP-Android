package info.free.scp.view.category

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import info.free.scp.R
import info.free.scp.SCPConstants.SCP_CONTEST
import info.free.scp.SCPConstants.SCP_CONTEST_CN
import info.free.scp.SCPConstants.SCP_EVENT
import info.free.scp.SCPConstants.SCP_SETTINGS
import info.free.scp.SCPConstants.SCP_SETTINGS_CN
import info.free.scp.SCPConstants.SCP_STORY_SERIES
import info.free.scp.SCPConstants.SCP_STORY_SERIES_CN
import info.free.scp.SCPConstants.SCP_TALES
import info.free.scp.SCPConstants.SCP_TALES_BY_TIME
import info.free.scp.SCPConstants.SCP_TALES_CN
import info.free.scp.SCPConstants.SERIES
import info.free.scp.SCPConstants.SERIES_ABOUT
import info.free.scp.SCPConstants.SERIES_ARCHIVED
import info.free.scp.SCPConstants.SERIES_CN
import info.free.scp.SCPConstants.SERIES_STORY
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.service.HttpManager
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import info.free.scp.view.WebActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {
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
        categoryType  = intent.getIntExtra("type", -1)
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
                categoryList.addAll(arrayOf("SCP系列1故事版1~499","SCP系列1故事版500~999","SCP系列1故事版1000~1499",
                        "SCP系列1故事版1499~1808", "SCP系列2故事版1~499", "SCP系列2故事版500~999", "SCP系列2故事版1000~1193",
                        "SCP系列3故事版1~499","SCP系列3故事版500~999","SCP系列3故事版1000~1211"))
            }
            SERIES_ARCHIVED -> {
                pageType = 0
                categoryList.addAll(arrayOf("搞笑SCP", "异常物品记录", "超常事件记录"))
                categoryList.addAll(if (isCnPage) arrayOf("已解明SCP") else arrayOf("前SCP", "被归档的SCP", "废除SCP", "删除SCP"))
            }
            SCP_TALES -> {
                // 1021
                pageType = 0
                categoryList.addAll(arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y","Z", "0-9"))
            }
            SCP_TALES_CN -> {
                // 1021
                pageType = 0
                categoryList.addAll(arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y","Z", "0-9"))
            }
            SERIES_ABOUT, SCP_STORY_SERIES, SCP_STORY_SERIES_CN, SCP_SETTINGS, SCP_SETTINGS_CN,
            SCP_CONTEST, SCP_CONTEST_CN, SCP_EVENT, SCP_TALES_BY_TIME -> {
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
                val start = if (position == 0) 0 else position*500 - 1
                val limit = if (start == 0) 499 else 500
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(categoryType, start, limit))
                scpAdapter?.notifyDataSetChanged()
                HttpManager.instance.getScpSeriesModel(start, limit) {
                    scpList.clear()
                    scpList.addAll(it)
                    for ((index, scp) in it.withIndex()) {
                        scp.type = SERIES
                        scp.index = start+index
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SERIES_CN -> {
                val start = if (position == 0) 0 else position*100 - 1
                val limit = if (start == 0) 99 else 100
                HttpManager.instance.getSeriesCnModel(start, limit) {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SERIES_STORY -> {
                when (position) {
                    // 故事版1~499
                    0 -> {
                        val start = 0
                        val limit = 499
                        HttpManager.instance.getFirstStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版1 500~999
                    1 -> {
                        val start = 499
                        val limit = 500
                        HttpManager.instance.getFirstStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版1 1000~1499
                    2 -> {
                        val start = 999
                        val limit = 500
                        HttpManager.instance.getFirstStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版1 1500~1808
                    3 -> {
                        val start = 1499
                        val limit = 500
                        HttpManager.instance.getFirstStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版2 1~499
                    4 -> {
                        val start = 0
                        val limit = 499
                        HttpManager.instance.getSecondStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版2 500~999
                    5 -> {
                        val start = 499
                        val limit = 500
                        HttpManager.instance.getSecondStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版2 1000~1193
                    6 -> {
                        val start = 999
                        val limit = 500
                        HttpManager.instance.getSecondStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版3 1~499
                    7 -> {
                        val start = 0
                        val limit = 499
                        HttpManager.instance.getThirdStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版3 500~999
                    8 -> {
                        val start = 499
                        val limit = 500
                        HttpManager.instance.getThirdStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    // 故事版3 1000~1211
                    9 -> {
                        val start = 999
                        val limit = 500
                        HttpManager.instance.getThirdStory(start, limit) {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
            SERIES_ABOUT -> {
                if (PreferenceUtil.getInit()) {
                    scpList.addAll(ScpDao.getInstance().getBasicInfo())
                } else {
                    ScpDao.getInstance().initBasicInfo()
                    scpList.addAll(ScpDao.getInstance().getBasicInfo())
                    PreferenceUtil.setInit()
                }
                scpAdapter?.notifyDataSetChanged()
            }
            SERIES_ARCHIVED -> {
                // 内容较少，直接全部加载
                when (position) {
                    0 -> {
                        // 搞笑scp
                        if (isCnPage) {
                            // cn
                            HttpManager.instance.getJokeCnModel {
                                scpList.addAll(it)
                                for (scp in it) {
                                    ScpDao.getInstance().replaceScpModel(scp)
                                }
                                scpAdapter?.notifyDataSetChanged()
                            }
                        } else {
                            HttpManager.instance.getJokeScp {
                                scpList.addAll(it)
                                for (scp in it) {
                                    ScpDao.getInstance().replaceScpModel(scp)
                                }
                                scpAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    3 -> {
                        // 已解明scp
                        HttpManager.instance.getExScp {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    4 -> {
                        // 归档scp
                        HttpManager.instance.getArchivedScp {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    5 -> {
                        // 废弃scp
                        HttpManager.instance.getDecommissionedScp {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    6 -> {
                        // 已移除scp
                        HttpManager.instance.getRemovedScp {
                            scpList.addAll(it)
                            for (scp in it) {
                                ScpDao.getInstance().replaceScpModel(scp)
                            }
                            scpAdapter?.notifyDataSetChanged()
                        }
                    }
                    else -> {
                        Toaster.show("开发中...")

                    }
                }
            }
            SCP_TALES -> {
                HttpManager.instance.getScpTales("{\"page_code\":\"${categoryList[position]}\"}") {
                    if (it.isEmpty()) {
                        Toaster.show("该项没有内容")
                        return@getScpTales
                    }
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_TALES_CN -> {
                HttpManager.instance.getCnScpTales("{\"page_code\":\"${categoryList[position]}\"}") {
                    if (it.isEmpty()) {
                        Toast.makeText(this, "该项没有内容", LENGTH_SHORT).show()
                        return@getCnScpTales
                    }
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_STORY_SERIES -> {
                HttpManager.instance.getStorySeries {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_STORY_SERIES_CN -> {
                HttpManager.instance.getCnStorySeries {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_SETTINGS -> {
                HttpManager.instance.getSettings {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_SETTINGS_CN -> {
                HttpManager.instance.getSettingsCn {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_CONTEST -> {
                HttpManager.instance.getContest {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_CONTEST_CN -> {
                HttpManager.instance.getContestCn {
                    scpList.addAll(it)
                    for (scp in it) {
                        ScpDao.getInstance().replaceScpModel(scp)
                    }
                    scpAdapter?.notifyDataSetChanged()
                }
            }
            SCP_EVENT -> { Toaster.show("开发中...") }
            SCP_TALES_BY_TIME -> { Toaster.show("开发中...") }
        }
    }
}
