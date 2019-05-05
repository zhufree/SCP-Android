package info.free.scp.view.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDatabase
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SearchActivity : BaseActivity() {
    var resultList: MutableList<ScpModel?> = emptyList<ScpModel>().toMutableList()
    var adapter: SearchResultAdapter? = null
    private var searchMode = 0 // 0 标题 1 全文

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initToolbar()

        val lm = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        rv_search_result?.layoutManager = lm
        btn_search?.setOnClickListener {
            val keyword = et_search_input?.text?.toString()?:""
            if (keyword.isEmpty()) {
                Toaster.show("请输入正确的关键词")
            } else {
                if (searchMode == 0) searchByTitle(keyword) else searchByDetail(keyword)
            }
        }

        tv_search_switch?.setOnClickListener {
            searchMode = if (searchMode == 0) 1 else 0
            tv_search_switch?.text = if (searchMode == 0) "标题搜索" else "全文搜索"
        }
    }

    private fun searchByTitle(keyword:String) {
        EventUtil.onEvent(this, EventUtil.searchTitle, keyword)
        PreferenceUtil.addPoints(5)
        resultList.clear()
        resultList.addAll(ScpDatabase.getInstance().scpDao().searchScpByTitle(keyword))
        if (resultList.size == 0) {
            Toaster.show("搜索结果为空")
        }
        if (adapter == null) {
            adapter = SearchResultAdapter(this@SearchActivity, resultList)
            rv_search_result?.adapter = adapter
            adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val intent = Intent()
                    intent.putExtra("link", resultList[position]?.link)
                    intent.putExtra("sId", resultList[position]?.sId)
                    intent.setClass(this@SearchActivity, DetailActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            adapter?.notifyDataSetChanged()
        }
    }
    private fun searchByDetail(keyword:String) {
        pb_searching?.visibility = VISIBLE
        doAsync {
            val scpList = ScpDatabase.getInstance().scpDao().searchScpByDetail("%$keyword%")
            uiThread {
                resultList.clear()
                resultList.addAll(scpList)
                Log.i("search", resultList.size.toString())
                if (adapter == null) {
                    adapter = SearchResultAdapter(this@SearchActivity, resultList)
                    rv_search_result?.adapter = adapter
                    adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            val intent = Intent()
                            intent.putExtra("link", resultList[position]?.link)
                            intent.putExtra("sId", resultList[position]?.sId)
                            intent.setClass(this@SearchActivity, DetailActivity::class.java)
                            startActivity(intent)
                        }
                    }
                } else {
                    adapter?.notifyDataSetChanged()
                }
                pb_searching?.visibility = GONE
            }
        }
//        Flowable.create<MutableList<ScpModel>>({emitter->
//            Log.i("search", "开始检索")
//            EventUtil.onEvent(this, EventUtil.searchDetail, keyword)
//            emitter.onNext(ScpDataHelper.getInstance().searchScpInDetailByKeyword(keyword))
//            emitter.onComplete()
//        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {scpList->
//                    resultList.clear()
//                    resultList.addAll(scpList)
//                    Log.i("search", resultList.size.toString())
//                    if (adapter == null) {
//                        adapter = SearchResultAdapter(this@SearchActivity, resultList)
//                        rv_search_result?.adapter = adapter
//                        adapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
//                            override fun onItemClick(view: View, position: Int) {
//                                val intent = Intent()
//                                intent.putExtra("link", resultList[position]?.link)
//                                intent.putExtra("sId", resultList[position]?.sId)
//                                intent.setClass(this@SearchActivity, DetailActivity::class.java)
//                                startActivity(intent)
//                            }
//                        }
//                    } else {
//                        adapter?.notifyDataSetChanged()
//                    }
//                    pb_searching?.visibility = GONE
//                }
        PreferenceUtil.addPoints(5)
    }

    private fun initToolbar() {
        setSupportActionBar(search_toolbar)
        search_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        search_toolbar?.setNavigationOnClickListener { finish() }
    }
}
