package info.free.scp.view.category

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.view.Menu
import android.view.View
import info.free.scp.R
import info.free.scp.SCPConstants.SERIES
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.service.HttpManager
import info.free.scp.view.WebActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {
    private val categoryList: MutableList<Int> = emptyList<Int>().toMutableList()
    private val scpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var categoryAdapter: CategoryAdapter? = null
    private var scpAdapter: ScpAdapter? = null
    private var pageType = 0 //一级目录和二级目录

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        categoryAdapter = CategoryAdapter(this, categoryList)
        val lm = LinearLayoutManager(this, VERTICAL, false)
        rlScpList.layoutManager = lm
        rlScpList.adapter = categoryAdapter
        categoryAdapter?.mOnItemClickListener = object: BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                pageType = 1
                if (scpAdapter == null) {
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
                }
                createRangeList(categoryList[position])
            }
        }


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
            }
            true
        }

    }

    private fun initData() {
        val categoryType  = intent.getIntExtra("type", -1)
        categoryList.clear()
        when (categoryType) {
            SERIES -> {
                categoryList.addAll(arrayOf(0, 500, 1000, 1500, 2000, 2500, 3000, 3500,
                4000, 4500).toMutableList())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.series_category_menu, menu)
        return true
    }

    private fun createRangeList(start: Int) {
        scpList.clear()
        HttpManager.instance.getAllScpSeriesModel(start, if (start == 0) 499 else 500) {
            scpList.addAll(it)
            for (scp in it) {
                ScpDao.getInstance().replaceScpModel(scp)
            }
            scpAdapter?.notifyDataSetChanged()
        }
    }
}
