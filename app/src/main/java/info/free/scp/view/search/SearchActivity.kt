package info.free.scp.view.search

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import info.free.scp.R
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseAdapter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : BaseActivity() {
    var resultList: MutableList<ScpModel?> = emptyList<ScpModel>().toMutableList()
    var adapter: SearchResultAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initToolbar()

        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_search_result?.layoutManager = lm
        btn_search?.setOnClickListener {
            val keyword = et_search_input?.text?.toString()?:""
            if (keyword.isEmpty()) {
                Toaster.show("请输入正确的关键词")
            } else {
                EventUtil.onEvent(this, EventUtil.doSearch, keyword)
                PreferenceUtil.addPoints(5)
                resultList.clear()
                resultList.addAll(ScpDao.getInstance().searchScpByKeyword(keyword))
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
            }
        }
        btn_search_detail?.setOnClickListener {
            val keyword = et_search_input?.text?.toString()?:""
            if (keyword.isEmpty()) {
                Toaster.show("请输入正确的关键词")
            } else {
                pb_searching?.visibility = VISIBLE
                Flowable.create<MutableList<ScpModel>>({emitter->
                    Log.i("search", "开始检索")
                    emitter.onNext(ScpDao.getInstance().searchScpInDetailByKeyword(keyword))
                    emitter.onComplete()
                }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {scpList->
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
                EventUtil.onEvent(this, EventUtil.doSearch, keyword)
                PreferenceUtil.addPoints(5)
            }
        }

    }

    private fun initToolbar() {
        setSupportActionBar(search_toolbar)
        search_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        search_toolbar?.setNavigationOnClickListener { finish() }
    }
}
