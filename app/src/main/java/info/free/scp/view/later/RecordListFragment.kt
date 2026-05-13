package info.free.scp.view.later

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.DESC
import info.free.scp.databinding.FragmentLaterListBinding
import info.free.scp.view.base.BaseFragment

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
            AlertDialog.Builder(requireContext())
                .setMessage("是否确认删除？")
                .setPositiveButton("确定") { _, _ ->
                    viewModel.deleteRecord(record)
                    adapter.submitList(viewModel.getRecordList(recordType, orderType))
                }
                .setNegativeButton("取消", null)
                .show()
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
