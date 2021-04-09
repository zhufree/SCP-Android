package info.free.scp.view.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.Entry.ART_DOC
import info.free.scp.SCPConstants.Entry.GOI_DOC
import info.free.scp.SCPConstants.Entry.INTERNATIONAL_DOC
import info.free.scp.SCPConstants.ScpType.SAVE_ART
import info.free.scp.SCPConstants.ScpType.SAVE_GOI
import info.free.scp.SCPConstants.ScpType.SAVE_INTERNATIONAL
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.activity_category_list.*
import kotlinx.android.synthetic.main.fragment_category.*

/**
 * 二级目录，点进去还有一层目录
 */
class CategoryFragment : BaseFragment() {
    private var entryType = -1
    private var subPosition = -1
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private val categoryList: MutableList<Any> = emptyList<Any>().toMutableList()
    private var currentCategoryPosition = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    private fun getSaveType(): Int {
        return when (entryType) {
            INTERNATIONAL_DOC -> SAVE_INTERNATIONAL
            GOI_DOC -> SAVE_GOI
            ART_DOC -> SAVE_ART
            else -> SAVE_INTERNATIONAL
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entryType = arguments?.getInt("entry_type") ?: -1
        val titleList = when (entryType) {
            INTERNATIONAL_DOC -> {
                listOf("俄国分部", "韩国分部", "法国分部", "波兰分部", "西班牙分部",
                        "泰国分部", "日本分部", "德国分部", "意大利分部", "乌克兰分部", "葡萄牙语分部",
                        "捷克分部", "非官方分部")
            }
            GOI_DOC -> {
                listOf("1-100", "100-200", "200-300", "300-400", "400+")
            }
            ART_DOC -> {
                listOf("1-100", "100-200", "200-300", "300+")
            }
            else -> {
                listOf("")
            }
        }
        val categoryAdapter = CategoryAdapter(getSaveType())
        rv_category_list.adapter = categoryAdapter
        categoryAdapter.submitList(titleList)

    }


    override fun onResume() {
        super.onResume()
    }

    companion object {

        fun newInstance(entryType: Int, subPosition: Int): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putInt("entry_type", entryType)
            args.putInt("sub_position", subPosition)
//            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}