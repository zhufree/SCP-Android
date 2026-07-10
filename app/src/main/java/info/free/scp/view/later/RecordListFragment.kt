package info.free.scp.view.later

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.DESC
import info.free.scp.bean.ScpRecordModel
import info.free.scp.ui.MainTheme
import info.free.scp.ui.later.RecordListScreen
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity

class RecordListFragment : BaseFragment() {
    private var recordType = LATER_TYPE
    private var orderType = DESC
    private var records by mutableStateOf<List<ScpRecordModel>>(emptyList())
    private var scrollToTopSignal by mutableIntStateOf(0)

    private val viewModel by lazy {
        ViewModelProvider(this)[LaterViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ComposeView {
        recordType = arguments?.getInt("record_type") ?: LATER_TYPE
        refreshList()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainTheme {
                    RecordListScreen(
                        records = records,
                        scrollToTopSignal = scrollToTopSignal,
                        onOpenRecord = { openRecord(it) },
                        onDeleteRecord = { deleteRecord(it) }
                    )
                }
            }
        }
    }

    fun refreshList() {
        records = viewModel.getRecordList(recordType, orderType)
    }

    fun reverseList(orderType: Int) {
        this.orderType = orderType
        records = viewModel.getRecordList(recordType, orderType)
        scrollToTopSignal += 1
    }

    private fun openRecord(record: ScpRecordModel) {
        startActivity(
            Intent(requireContext(), DetailActivity::class.java)
                .putExtra("link", record.link)
                .putExtra("title", record.link)
        )
    }

    private fun deleteRecord(record: ScpRecordModel) {
        viewModel.deleteRecord(record)
        refreshList()
    }

    companion object {
        fun newInstance(recordType: Int): RecordListFragment {
            val fragment = RecordListFragment()
            val args = Bundle()
            args.putInt("record_type", recordType)
            fragment.arguments = args
            return fragment
        }
    }
}
