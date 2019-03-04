package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDao
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseAdapter
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.fragment_category.*

/**
 * 单个排布的scp， 用于相关材料，多个单页面
 */
class ScpItemFragment : BaseFragment() {
    private var categoryType = -1
    private var clickPosition = -1
    private val scpList: MutableList<ScpModel?>? = emptyList<ScpModel>().toMutableList()
    private var scpAdapter: ScpAdapter? = null
    private var currentScrollPosition = -1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lm = androidx.recyclerview.widget.GridLayoutManager(mContext, 2, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        rv_category_list?.layoutManager = lm
        initScpItemAdapter()
    }

    private fun initScpItemAdapter() {
        categoryType = arguments?.getInt("category_type") ?: -1
        clickPosition = arguments?.getInt("click_position") ?: -1
        Log.i(tag, "初始化scpAdapter")
        getScpList()
        scpList?.let {
            scpAdapter = ScpAdapter(mContext!!, it)
            rv_category_list?.adapter = scpAdapter
        }
        scpAdapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                scpList?.let {
                    PreferenceUtil.addPoints(2)
                    val intent = Intent()
                    intent.putExtra("link", it[position]?.link)
                    intent.putExtra("sId", it[position]?.sId)
                    intent.setClass(mContext, DetailActivity::class.java)
                    startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)
                }
            }
        }
    }

    private fun getScpList() {
        Log.i("category", "加载scp列表")
        scpList?.clear()
        when (categoryType) {
            SCPConstants.Category.ABOUT_INFO -> {
                // 相关材料
                scpList?.addAll(ScpDao.getInstance().getSinglePageByType(SCPConstants.ScpType.SAVE_INFO))
            }
        }
        if (scpList?.size == 0) {
            Toaster.show("该页没有内容或数据加载未完成")
        }
        scpAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        if (currentScrollPosition > -1 && currentScrollPosition < scpList?.size ?: 0) {
            rv_category_list?.scrollToPosition(currentScrollPosition)
        }
    }


    companion object {

        fun newInstance(categoryType: Int, clickPosition: Int): ScpItemFragment {
            val fragment = ScpItemFragment()
            val args = Bundle()
            args.putInt("category_type", categoryType)
            args.putInt("click_position", clickPosition)
            //            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}