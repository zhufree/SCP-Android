package info.free.scp.view.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.databinding.FragmentScpListBinding
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_scp_list.*

/**
 * 一级目录，点进去是正文
 */
class ScpListFragment : BaseFragment() {
    private lateinit var binding: FragmentScpListBinding

    private val vm by lazy {
        ViewModelProvider(this)
                .get(GroupViewModel::class.java)
    }

    private val docAdapter by lazy {
        ScpAdapter()
    }

    private var saveType = 1
    private var groupIndex = -1
    private var extraType = ""


    private fun getDocList(saveType: Int, groupIndex: Int, extraType: String): List<ScpModel> {
        return when (saveType) {
            // SCP系列
            SCPConstants.ScpType.SAVE_SERIES, SCPConstants.ScpType.SAVE_SERIES_CN, SCPConstants.ScpType.SAVE_JOKE, SCPConstants.ScpType.SAVE_JOKE_CN, SCPConstants.ScpType.SAVE_EX, SCPConstants.ScpType.SAVE_EX_CN -> {
                vm.getDocList(saveType, groupIndex)
            }
            // 故事
            SCPConstants.ScpType.SAVE_TALES, SCPConstants.ScpType.SAVE_TALES_CN,
            SCPConstants.ScpType.SAVE_TALES_BY_TIME, SCPConstants.ScpType.SAVE_WANDER,
            SCPConstants.ScpType.SAVE_WANDER_CN -> {
                vm.getDocList(saveType, extraType = extraType)
            }
            SCPConstants.ScpType.SAVE_CANON, SCPConstants.ScpType.SAVE_CANON_CN,
            SCPConstants.ScpType.SAVE_STORY_SERIES, SCPConstants.ScpType.SAVE_STORY_SERIES_CN,
            SCPConstants.ScpType.SAVE_CONTEST, SCPConstants.ScpType.SAVE_CONTEST_CN -> {
                vm.getDocList(saveType)
            }
            // 图书馆
            SCPConstants.Entry.LIBRARY_DOC -> {
                vm.getDocList(saveType)
            }
            SCPConstants.ScpType.SAVE_INTERNATIONAL -> {
                vm.getDocList(saveType, extraType = extraType)
            }
            else -> {
                vm.getDocList(saveType, groupIndex)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentScpListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveType = arguments?.getInt("save_type") ?: -1
        groupIndex = arguments?.getInt("group_index") ?: -1
        extraType = arguments?.getString("extra_type") ?: ""

        rv_doc_list.adapter = docAdapter
        val docList = getDocList(saveType, groupIndex, extraType)
        docAdapter.submitList(docList)
    }

    override fun refreshTheme() {
        super.refreshTheme()
        docAdapter.refreshTheme()
    }

    override fun onResume() {
        super.onResume()
        val docList = getDocList(saveType, groupIndex, extraType)
        docAdapter.submitList(docList)
    }

    fun reverseScpList(orderType: Int) {
        val ascList = getDocList(saveType, groupIndex, extraType)
        docAdapter.submitList(if (orderType == SCPConstants.OrderType.ASC) ascList else ascList.reversed()) {
            rv_doc_list.scrollToPosition(0)
        }
    }


    companion object {

        fun newInstance(saveType: Int, groupIndex: Int, extraType: String): ScpListFragment {
            val fragment = ScpListFragment()
            val args = Bundle()
            args.putInt("save_type", saveType)
            args.putInt("group_index", groupIndex)
            args.putString("extra_type", extraType)
            //            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}