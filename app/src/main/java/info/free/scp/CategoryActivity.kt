package info.free.scp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.view.Menu
import android.view.View
import info.free.scp.bean.ScpModel
import info.free.scp.service.HttpManager
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {
    private val scpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var mAdapter: CategoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        mAdapter = CategoryAdapter(this, scpList)
        val lm = LinearLayoutManager(this, VERTICAL, false)
        rlScpList.layoutManager = lm
        rlScpList.adapter = mAdapter
        mAdapter?.mOnItemClickListener = object: BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent()
                intent.putExtra("link", scpList[position].link)
                intent.setClass(this@CategoryActivity, WebActivity::class.java)
                startActivity(intent)
            }
        }
        createRangeList(1, 999)

        toolbar.inflateMenu(R.menu.series_category_menu) //设置右上角的填充菜单
        toolbar.setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.thousand1 -> {
                    createRangeList(1, 300)
                }
                R.id.thousand2 -> {
                    createRangeList(1000, 1999)
                }
                R.id.thousand3 -> {
                    createRangeList(2000, 2999)
                }
                R.id.thousand4 -> {
                    createRangeList(3000, 3999)
                }
                R.id.thousand5 -> {
                    createRangeList(4000, 4999)
                }
            }
            true

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.series_category_menu, menu)
        return true
    }

    private fun createRangeList(start: Int, end: Int) {
        scpList.clear()
        HttpManager.instance.getAllScpSeriesModel(start-1, end - start) {
            scpList.addAll(it)
            mAdapter?.notifyDataSetChanged()
        }
    }
}
