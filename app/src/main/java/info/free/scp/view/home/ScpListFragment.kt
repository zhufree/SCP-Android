package info.free.scp.view.home

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
import info.free.scp.view.category.ScpAdapter
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.fragment_category.*

class ScpListFragment : BaseFragment() {
    private var categoryType = -1
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val scpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private var scpAdapter: ScpAdapter? = null
    private val eventScpList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    private val taleTimeList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()


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
        categoryType = arguments?.getInt("category_type")?:-1
        if (scpAdapter == null) {
            Log.i(tag, "初始化scpAdapter")
            getScpList(categoryType)
            scpAdapter = ScpAdapter(mContext!!, scpList)
            scpAdapter?.mOnItemClickListener = object : BaseAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    PreferenceUtil.addPoints(2)
                    val intent = Intent()
                    intent.putExtra("link", scpList[position].link)
                    intent.putExtra("sId", scpList[position].sId)
                    intent.setClass(mContext, DetailActivity::class.java)
                    startActivityForResult(intent, SCPConstants.RequestCode.CATEGORY_TO_DETAIL)
                }
            }
        }
        rv_category_list?.adapter = scpAdapter
        scpAdapter?.notifyDataSetChanged()
    }

    private fun getScpList(position: Int) {
        Log.i("category", "加载scp列表")
        scpList.clear()
        when (categoryType) {
            SCPConstants.Category.SERIES -> {
                // 0,499,999
                val start = if (position == 0) 0 else position * categoryCount
                val limit = categoryCount
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SCPConstants.SaveType.SAVE_SERIES, start, limit))
            }
            SCPConstants.Category.SERIES_CN -> {
                val start = if (position == 0) 0 else position * categoryCount
                val limit = categoryCount
                scpList.addAll(ScpDao.getInstance().getScpByTypeAndRange(SCPConstants.SaveType.SAVE_SERIES_CN, start, limit))

            }
            SCPConstants.Category.SCP_EX -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_EX))
            }
            SCPConstants.Category.SERIES_ABOUT -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_ABOUT))
            }
            SCPConstants.Category.SERIES_ARCHIVED -> {
                // 内容较少，直接全部加载
                when (position) {
                    0 -> {
                        // 搞笑scp
//                        if (isCnPage) {
                            // cn
                            scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_JOKE_CN))

//                        } else {
                            scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_JOKE))
//                        }
                    }
                }
            }
            SCPConstants.Category.SCP_ARCHIVED -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_ARCHIVED))
            }
            SCPConstants.Category.SCP_DECOMMISSIONED -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_DECOMMISSIONED))
            }
            SCPConstants.Category.SCP_REMOVED -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_REMOVED))
            }
            SCPConstants.Category.TALES -> {
//                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_TALES_PREFIX + categoryList[position]))
            }
            SCPConstants.Category.TALES_CN -> {
//                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_TALES_CN_PREFIX + categoryList[position]))
            }
            SCPConstants.Category.STORY_SERIES -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_STORY_SERIES))
            }
            SCPConstants.Category.STORY_SERIES_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_STORY_SERIES_CN))
            }
            SCPConstants.Category.SETTINGS -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_SETTINGS))
            }
            SCPConstants.Category.SETTINGS_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_SETTINGS_CN))
            }
            SCPConstants.Category.CONTEST -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_CONTEST))
            }
            SCPConstants.Category.CONTEST_CN -> {
                scpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_CONTEST_CN))
            }
            SCPConstants.Category.EVENT -> {
                if (eventScpList.isEmpty()) {
                    eventScpList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_EVENT))
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
            SCPConstants.Category.TALES_BY_TIME -> {
                if (taleTimeList.isEmpty()) {
                    taleTimeList.addAll(ScpDao.getInstance().getScpByType(SCPConstants.SaveType.SAVE_TALES_BY_TIME))
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

    fun refreshTheme() {

    }

    companion object {

        fun newInstance(categoryType: Int): ScpListFragment {
            val fragment = ScpListFragment()
            val args = Bundle()
            args.putInt("category_type", categoryType)
//            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}