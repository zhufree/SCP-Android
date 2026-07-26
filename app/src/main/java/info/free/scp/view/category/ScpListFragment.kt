package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.ui.MainTheme
import info.free.scp.ui.category.ScpDocumentListScreen
import info.free.scp.ui.category.ScpDocumentStatus
import info.free.scp.util.EventUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity

class ScpListFragment : BaseFragment() {
    private val vm by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    private var saveType = 1
    private var groupIndex = -1
    private var extraType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveType = arguments?.getInt("save_type") ?: -1
        groupIndex = arguments?.getInt("group_index") ?: -1
        extraType = arguments?.getString("extra_type") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        vm.loadDocuments(saveType, groupIndex, extraType)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by vm.categoryUiState.collectAsState()
                MainTheme {
                    ScpDocumentListScreen(
                        documents = uiState.documents,
                        scrollToTopSignal = uiState.scrollToTopSignal,
                        statusProvider = vm::getDocumentStatus,
                        onOpenDocument = { scp, index -> openDocument(scp, index) },
                        onToggleRead = { scp -> toggleRead(scp) },
                        onToggleLater = { scp -> toggleLater(scp) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.loadDocuments(saveType, groupIndex, extraType)
    }

    fun reverseScpList(orderType: Int) {
        vm.setDocumentOrder(orderType)
    }

    private fun openDocument(scp: ScpModel, index: Int) {
        vm.rewardDocumentOpen()
        startActivity(
            Intent(requireContext(), DetailActivity::class.java)
                .putExtra("link", scp.link)
                .putExtra("title", scp.title)
                .putExtra("index", scp.index)
                .putExtra("scp_type", scp.scpType)
        )
    }

    private fun toggleRead(scp: ScpModel) {
        vm.toggleRead(scp)
    }

    private fun toggleLater(scp: ScpModel) {
        val isInLater = vm.toggleLater(scp)
        if (isInLater) {
            EventUtil.onEvent(requireContext(), EventUtil.addLater)
        }
    }

    companion object {
        fun newInstance(saveType: Int, groupIndex: Int, extraType: String): ScpListFragment {
            val fragment = ScpListFragment()
            val args = Bundle()
            args.putInt("save_type", saveType)
            args.putInt("group_index", groupIndex)
            args.putString("extra_type", extraType)
            fragment.arguments = args
            return fragment
        }
    }
}
