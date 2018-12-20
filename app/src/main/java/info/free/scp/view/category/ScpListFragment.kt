package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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

class ScpListFragment : BaseFragment() {
    private var categoryType = -1
    private var clickPosition = -1
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val scpList: MutableList<ScpModel>? = emptyList<ScpModel>().toMutableList()
    private var scpAdapter: ScpAdapter? = null
    private val eventScpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private val taleTimeList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var currentScrollPosition = -1

    private val taleCategory = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lm = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        rv_category_list?.layoutManager = lm
        initScpAdapter()
    }

    private fun initScpAdapter() {
        categoryType = arguments?.getInt("category_type") ?: -1
        clickPosition = arguments?.getInt("click_position") ?: -1
        if (scpAdapter == null) {
            Log.i(tag, "初始化scpAdapter")
            getScpList()
            scpList?.let {
                scpAdapter = ScpAdapter(mContext!!, it)
            }
            scpAdapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    scpList?.let {
                        PreferenceUtil.addPoints(2)
                        val intent = Intent()
                        intent.putExtra("link", it[position].link)
                        intent.putExtra("sId", it[position].sId)
                        intent.setClass(mContext, DetailActivity::class.java)
                        startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)
                    }
                }
            }
        }
        rv_category_list?.adapter = scpAdapter
        scpAdapter?.notifyDataSetChanged()
    }

    private fun getScpList() {
        Log.i("category", "加载scp列表")
        scpList?.clear()
        when (categoryType) {
            SCPConstants.Category.SERIES -> {
                // 0,499,999
                val start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                val limit = categoryCount
                scpList?.addAll(ScpDao.getInstance().getScpByTypeAndRange(SCPConstants.SaveType.SAVE_SERIES, start, limit))
            }
            SCPConstants.Category.SERIES_CN -> {
                val start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                val limit = categoryCount
                scpList?.addAll(ScpDao.getInstance().getScpByTypeAndRange(SCPConstants.SaveType.SAVE_SERIES_CN, start, limit))

            }
            SCPConstants.Category.SCP_EX -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_EX))
            }
            SCPConstants.Category.SERIES_ABOUT -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_ABOUT))
            }

            SCPConstants.Category.SCP_ARCHIVED -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_ARCHIVED))
            }
            SCPConstants.Category.SCP_DECOMMISSIONED -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_DECOMMISSIONED))
            }
            SCPConstants.Category.SCP_REMOVED -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_REMOVED))
            }
            SCPConstants.Category.TALES -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_TALES_PREFIX
                        + taleCategory[clickPosition]))
            }
            SCPConstants.Category.TALES_CN -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_TALES_CN_PREFIX
                        + taleCategory[clickPosition]))
            }
            SCPConstants.Category.STORY_SERIES -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_STORY_SERIES))
            }
            SCPConstants.Category.STORY_SERIES_CN -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_STORY_SERIES_CN))
            }
            SCPConstants.Category.JOKE -> {
                // 内容较少，直接全部加载
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_JOKE))
            }
            SCPConstants.Category.JOKE_CN -> {
                // 内容较少，直接全部加载
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_JOKE_CN))
            }
            SCPConstants.Category.SETTINGS -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_SETTINGS))
            }
            SCPConstants.Category.SETTINGS_CN -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_SETTINGS_CN))
            }
            SCPConstants.Category.CONTEST -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_CONTEST))
            }
            SCPConstants.Category.CONTEST_CN -> {
                scpList?.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_CONTEST_CN))
            }
            SCPConstants.Category.TALES_BY_TIME -> {
                if (taleTimeList.isEmpty()) {
                    taleTimeList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_TALES_BY_TIME))
                }
                when (clickPosition) {
                    0 -> {
                        scpList?.addAll(taleTimeList.filter { it.month.startsWith("2018") })
                    }
                    1 -> {
                        scpList?.addAll(taleTimeList.filter { it.month.startsWith("2017") })
                    }
                    2 -> {
                        scpList?.addAll(taleTimeList.filter { it.month.startsWith("2016") })
                    }
                    3 -> {
                        scpList?.addAll(taleTimeList.filter { it.month.startsWith("2015") })
                    }
                    4 -> {
                        scpList?.addAll(taleTimeList.filter { it.month.startsWith("2014") })
                    }
                }

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

    fun reverseScpList() {
        scpList?.reverse()
        scpAdapter?.notifyDataSetChanged()
    }

    fun refreshTheme() {

    }

    companion object {

        fun newInstance(categoryType: Int, clickPosition: Int): ScpListFragment {
            val fragment = ScpListFragment()
            val args = Bundle()
            args.putInt("category_type", categoryType)
            args.putInt("click_position", clickPosition)
            //            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}