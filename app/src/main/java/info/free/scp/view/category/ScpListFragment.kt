package info.free.scp.view.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.databinding.FragmentCategoryBinding
import info.free.scp.db.ScpDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_category.*
import org.jetbrains.anko.support.v4.toast

/**
 * 一级目录，点进去是正文
 */
class ScpListFragment : BaseFragment() {
    private var categoryType = -1
    private var clickPosition = -1
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val localScpList: MutableList<ScpModel?>? = emptyList<ScpModel>().toMutableList()
    private val taleTimeList: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()

    private val taleCategory = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0-9")
    private lateinit var binding: FragmentCategoryBinding
    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(CategoryViewModel::class.java)
    }
    private val adapter by lazy {
        ScpAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryType = arguments?.getInt("category_type") ?: -1
        clickPosition = arguments?.getInt("click_position") ?: -1

        viewModel.getCat()?.observe(viewLifecycleOwner, Observer { result ->
            binding.slCategory.isRefreshing = false
            if (result != null && result.isNotEmpty()) adapter.submitList(result)
        })
        binding.rvCategoryList.adapter = adapter
        if (PreferenceUtil.getAppMode() == ONLINE) {
            binding.slCategory.isRefreshing = true
        }
        if (PreferenceUtil.getAppMode() == ONLINE) {
            viewModel.loadCat(categoryType, clickPosition)
        } else {
            getScpListOffline()
            adapter.submitList(localScpList)
        }
        binding.slCategory.setOnRefreshListener {
            viewModel.loadCat(categoryType, clickPosition)
        }
    }

    private fun getScpListOffline() {
        Log.i("category", "加载scp列表")
        localScpList?.clear()
        when (categoryType) {
            SCPConstants.Category.SERIES -> {
                // 0,499,999
                val start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                val limit = categoryCount
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByTypeAndRange(SCPConstants.ScpType.SAVE_SERIES, start, limit))
            }
            SCPConstants.Category.SERIES_CN -> {
                val start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                val limit = categoryCount
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByTypeAndRange(SCPConstants.ScpType.SAVE_SERIES_CN, start, limit))

            }
            SCPConstants.Category.SCP_EX -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_EX))
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_EX_CN))
            }
            SCPConstants.Category.SCP_INTERNATIONAL -> {
                when (clickPosition) {
                    0 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("俄国分部"))
                    1 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("韩国分部"))
                    2 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("法国分部"))
                    3 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("波兰分部"))
                    4 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("西班牙分部"))
                    5 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("泰国分部"))
                    6 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("日本分部"))
                    7 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("德国分部"))
                    8 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("意大利分部"))
                    9 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("乌克兰分部"))
                    10 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("葡萄牙语分部"))
                    11 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("捷克分部"))
                    12 -> localScpList?.addAll(ScpDataHelper.getInstance().getInternationalByCountry("非官方分部"))

                }
            }
            SCPConstants.Category.SCP_ABNORMAL -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getSinglePageByType(SCPConstants.ScpType.SAVE_ABNORMAL))
                // 三句话外围
                localScpList?.add(ScpDatabase.getInstance()?.scpDao()?.getScpByLink("/short-stories"))
            }
            SCPConstants.Category.ABOUT_INFO -> {
                // 相关材料
                localScpList?.addAll(ScpDataHelper.getInstance().getSinglePageByType(SCPConstants.ScpType.SAVE_INFO))
            }
            SCPConstants.Category.ABOUT_INTRO -> {
                // 相关材料
                localScpList?.addAll(ScpDataHelper.getInstance().getSinglePageByType(SCPConstants.ScpType.SAVE_INTRO))
            }

            SCPConstants.Category.SCP_ARCHIVES -> {
                when (clickPosition) {
                    0 -> localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_ARCHIVED))
                    1 -> localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_DECOMMISSIONED))
                    2 -> localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_REMOVED))
                }
            }
            SCPConstants.Category.TALES -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getTalesByTypeAndSubType(SCPConstants.ScpType.SAVE_TALES
                        , taleCategory[clickPosition]))
            }
            SCPConstants.Category.TALES_CN -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getTalesByTypeAndSubType(SCPConstants.ScpType.SAVE_TALES_CN
                        , taleCategory[clickPosition]))
            }
            SCPConstants.Category.STORY_SERIES -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_STORY_SERIES))
            }
            SCPConstants.Category.STORY_SERIES_CN -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_STORY_SERIES_CN))
            }
            SCPConstants.Category.JOKE -> {
                // 内容较少，直接全部加载
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_JOKE))
            }
            SCPConstants.Category.JOKE_CN -> {
                // 内容较少，直接全部加载
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_JOKE_CN))
            }
            SCPConstants.Category.SETTINGS -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_SETTINGS))
            }
            SCPConstants.Category.SETTINGS_CN -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_SETTINGS_CN))
            }
            SCPConstants.Category.CONTEST -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_CONTEST))
            }
            SCPConstants.Category.CONTEST_CN -> {
                localScpList?.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_CONTEST_CN))
            }

            SCPConstants.Category.TALES_BY_TIME -> {
                if (taleTimeList.isEmpty()) {
                    taleTimeList.addAll(ScpDataHelper.getInstance().getScpByType(SCPConstants.ScpType.SAVE_TALES_CN))
                }
                when (clickPosition) {
                    0 -> {
                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2018") == true })
                    }
                    1 -> {
                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2017") == true })
                    }
                    2 -> {
                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2016") == true })
                    }
                    3 -> {
                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2015") == true })
                    }
                    4 -> {
                        localScpList?.addAll(taleTimeList.filter { (it as ScpItemModel).subScpType?.startsWith("2014") == true })
                    }
                }

            }
        }
        if (localScpList?.size == 0) {
            if (categoryType == SCPConstants.Category.SCP_INTERNATIONAL) {
                toast("该页没有内容或数据加载未完成，请检查数据库是否是最新版本")
            } else {
                toast("该页没有内容或数据加载未完成")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter.currentScrollPosition > -1 && adapter.currentScrollPosition < localScpList?.size ?: 0) {
            rv_category_list?.scrollToPosition(adapter.currentScrollPosition)
        }
    }

    fun reverseScpList() {
        localScpList?.reverse()
        viewModel.reverseCat()
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