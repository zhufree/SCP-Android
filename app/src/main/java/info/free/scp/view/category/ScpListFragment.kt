package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.data.settings.InteractionPointsStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.ui.MainTheme
import info.free.scp.ui.category.ScpDocumentListScreen
import info.free.scp.ui.category.ScpDocumentStatus
import info.free.scp.util.EventUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity

class ScpListFragment : BaseFragment() {
    private val interactionPoints: InteractionPointsStore = PreferenceSettingsStore()
    private val vm by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    private val readingListRepository = ReadingListRepository()

    private var saveType = 1
    private var groupIndex = -1
    private var extraType = ""
    private var docList by mutableStateOf<List<ScpModel>>(emptyList())
    private var scrollToTopSignal by mutableIntStateOf(0)

    private fun getDocList(saveType: Int, groupIndex: Int, extraType: String): List<ScpModel> {
        return when (saveType) {
            SCPConstants.ScpType.SAVE_SERIES,
            SCPConstants.ScpType.SAVE_SERIES_CN,
            SCPConstants.ScpType.SAVE_JOKE,
            SCPConstants.ScpType.SAVE_JOKE_CN,
            SCPConstants.ScpType.SAVE_EX,
            SCPConstants.ScpType.SAVE_EX_CN -> {
                vm.getDocList(saveType, groupIndex)
            }
            SCPConstants.ScpType.SAVE_TALES,
            SCPConstants.ScpType.SAVE_TALES_CN,
            SCPConstants.ScpType.SAVE_TALES_BY_TIME,
            SCPConstants.ScpType.SAVE_WANDER,
            SCPConstants.ScpType.SAVE_WANDER_CN -> {
                vm.getDocList(saveType, extraType = extraType)
            }
            SCPConstants.ScpType.SAVE_CANON,
            SCPConstants.ScpType.SAVE_CANON_CN,
            SCPConstants.ScpType.SAVE_STORY_SERIES,
            SCPConstants.ScpType.SAVE_STORY_SERIES_CN,
            SCPConstants.ScpType.SAVE_CONTEST,
            SCPConstants.ScpType.SAVE_CONTEST_CN,
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
        refreshDocList()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainTheme {
                    ScpDocumentListScreen(
                        documents = docList,
                        scrollToTopSignal = scrollToTopSignal,
                        statusProvider = { scp -> getDocumentStatus(scp) },
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
        refreshDocList()
    }

    fun reverseScpList(orderType: Int) {
        val ascList = getDocList(saveType, groupIndex, extraType)
        docList = if (orderType == SCPConstants.OrderType.ASC) ascList else ascList.reversed()
        scrollToTopSignal += 1
    }

    private fun refreshDocList() {
        docList = getDocList(saveType, groupIndex, extraType)
    }

    private fun getDocumentStatus(scp: ScpModel): ScpDocumentStatus {
        val status = readingListRepository.getDocumentStatus(scp.link)
        return ScpDocumentStatus(
            isLiked = status.isLiked,
            hasRead = status.hasRead,
            isInLater = status.isInLater
        )
    }

    private fun openDocument(scp: ScpModel, index: Int) {
        interactionPoints.increasePoints(2)
        startActivity(
            Intent(requireContext(), DetailActivity::class.java)
                .putExtra("link", scp.link)
                .putExtra("title", scp.title)
                .putExtra("index", scp.index)
                .putExtra("scp_type", scp.scpType)
        )
    }

    private fun toggleRead(scp: ScpModel) {
        readingListRepository.toggleRead(scp.link, scp.title)
    }

    private fun toggleLater(scp: ScpModel) {
        val isInLater = readingListRepository.toggleLater(scp.link, scp.title)
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
