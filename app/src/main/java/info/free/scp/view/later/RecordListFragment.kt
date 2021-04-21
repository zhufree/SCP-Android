package info.free.scp.view.later

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.DESC
import info.free.scp.databinding.FragmentLaterListBinding
import info.free.scp.view.base.BaseFragment
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.yesButton

/**
 * 待读和历史列表共用一个fragment
 * recordType区分
 */
class RecordListFragment : BaseFragment() {

    var recordType = LATER_TYPE
    var orderType = DESC

    companion object {
        fun newInstance(recordType: Int): RecordListFragment {
            val fragment = RecordListFragment()
            val args = Bundle()
            args.putInt("record_type", recordType)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(LaterViewModel::class.java)
    }

    private val laterAdapter by lazy {
        RecordListAdapter()
    }

    private lateinit var binding: FragmentLaterListBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaterListBinding.inflate(inflater, container, false)
        binding.rvLater.adapter = laterAdapter
        subscribeUi(laterAdapter)
        return binding.root
    }


    private fun subscribeUi(adapter: RecordListAdapter) {
        recordType = arguments?.getInt("record_type") ?: LATER_TYPE
        adapter.submitList(viewModel.getRecordList(recordType, orderType))
        adapter.onLongClick = { record ->
            alert("是否确认删除？") {
                yesButton {
                    viewModel.deleteRecord(record)
                    adapter.submitList(viewModel.getRecordList(recordType, orderType))
                }
                noButton { }
            }.show()
        }
    }

    fun refreshList() {
        laterAdapter.submitList(viewModel.getRecordList(recordType, orderType))
    }

    fun reverseList(orderType: Int) {
        laterAdapter.submitList(viewModel.getRecordList(recordType, orderType)) {
            binding.rvLater.scrollToPosition(0)
        }
    }


    override fun refreshTheme() {
        super.refreshTheme()
        laterAdapter.refreshTheme()
//        refreshList()
    }
}
