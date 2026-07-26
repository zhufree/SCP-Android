package info.free.scp.view.category

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.SCPConstants.ScpType.SAVE_CANON
import info.free.scp.SCPConstants.ScpType.SAVE_CANON_CN
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST
import info.free.scp.SCPConstants.ScpType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.ScpType.SAVE_EX
import info.free.scp.SCPConstants.ScpType.SAVE_EX_CN
import info.free.scp.SCPConstants.ScpType.SAVE_INTERNATIONAL
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_TALES
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.ScpType.SAVE_TALES_CN
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER
import info.free.scp.SCPConstants.ScpType.SAVE_WANDER_CN
import info.free.scp.bean.ScpModel
import info.free.scp.components.CommonTopBar
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.data.settings.InteractionPointsStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.ui.MainTheme
import info.free.scp.ui.category.ScpDocumentListScreen
import info.free.scp.ui.category.ScpDocumentStatus
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity

class DocListActivity : BaseActivity() {
    private val interactionPoints: InteractionPointsStore = PreferenceSettingsStore()
    private val vm by viewModels<GroupViewModel>()
    private val readingListRepository = ReadingListRepository()

    private var saveType = 1
    private var groupIndex = -1
    private var extraType = ""
    private var orderType = ASC
    private var docList by mutableStateOf<List<ScpModel>>(emptyList())
    private var scrollToTopSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveType = intent.getIntExtra("saveType", 1)
        groupIndex = intent.getIntExtra("groupIndex", -1)
        extraType = intent.getStringExtra("extraType") ?: ""
        refreshDocList()

        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getListTitle(saveType),
                            actions = {
                                IconButton(onClick = { reverseScpList() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_swap_vertical_circle_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Reverse"
                                    )
                                }
                            },
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
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
    }

    override fun onResume() {
        super.onResume()
        refreshDocList()
    }

    private fun getListTitle(saveType: Int): String {
        return when (saveType) {
            SAVE_SERIES -> "SCP系列"
            SAVE_SERIES_CN -> "SCP-CN系列"
            SAVE_JOKE -> "搞笑SCP"
            SAVE_JOKE_CN -> "CN搞笑SCP"
            SAVE_EX -> "已解明SCP"
            SAVE_EX_CN -> "CN已解明SCP"
            SAVE_TALES -> "基金会故事"
            SAVE_TALES_CN -> "CN原创故事"
            SAVE_TALES_BY_TIME -> "CN原创故事(按发布时间排列)"
            SAVE_CANON -> "设定中心"
            SAVE_CANON_CN -> "CN设定中心"
            SAVE_STORY_SERIES -> "故事系列"
            SAVE_STORY_SERIES_CN -> "CN故事系列"
            SAVE_CONTEST -> "征文竞赛"
            SAVE_CONTEST_CN -> "CN征文竞赛"
            SAVE_WANDER -> "放逐者图书馆"
            SAVE_WANDER_CN -> "CN放逐者图书馆"
            else -> "SCP系列"
        }
    }

    private fun getDocList(saveType: Int, groupIndex: Int, extraType: String): List<ScpModel> {
        return when (saveType) {
            SAVE_SERIES, SAVE_SERIES_CN, SAVE_JOKE, SAVE_JOKE_CN, SAVE_EX, SAVE_EX_CN -> {
                vm.getDocList(saveType, groupIndex)
            }
            SAVE_TALES, SAVE_TALES_CN, SAVE_TALES_BY_TIME, SAVE_WANDER, SAVE_WANDER_CN -> {
                vm.getDocList(saveType, extraType = extraType)
            }
            SAVE_CANON, SAVE_CANON_CN, SAVE_STORY_SERIES, SAVE_STORY_SERIES_CN,
            SAVE_CONTEST, SAVE_CONTEST_CN, SCPConstants.Entry.LIBRARY_DOC -> {
                vm.getDocList(saveType)
            }
            SAVE_INTERNATIONAL -> {
                vm.getDocList(saveType, extraType = extraType)
            }
            else -> {
                vm.getDocList(saveType, groupIndex)
            }
        }
    }

    private fun refreshDocList() {
        val ascList = getDocList(saveType, groupIndex, extraType)
        docList = if (orderType == ASC) ascList else ascList.reversed()
    }

    private fun reverseScpList() {
        orderType = -orderType
        refreshDocList()
        scrollToTopSignal += 1
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
            Intent(this, DetailActivity::class.java)
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
            EventUtil.onEvent(this, EventUtil.addLater)
        }
    }
}
