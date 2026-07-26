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
import androidx.compose.runtime.collectAsState
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
import info.free.scp.ui.MainTheme
import info.free.scp.ui.category.ScpDocumentListScreen
import info.free.scp.ui.category.ScpDocumentStatus
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity

class DocListActivity : BaseActivity() {
    private val vm by viewModels<GroupViewModel>()

    private var saveType = 1
    private var groupIndex = -1
    private var extraType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveType = intent.getIntExtra("saveType", 1)
        groupIndex = intent.getIntExtra("groupIndex", -1)
        extraType = intent.getStringExtra("extraType") ?: ""
        vm.loadDocuments(saveType, groupIndex, extraType)

        setContent {
            val uiState by vm.categoryUiState.collectAsState()
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getListTitle(saveType),
                            actions = {
                                IconButton(onClick = { vm.reverseDocumentOrder() }) {
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
    }

    override fun onResume() {
        super.onResume()
        vm.loadDocuments(saveType, groupIndex, extraType)
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

    private fun openDocument(scp: ScpModel, index: Int) {
        vm.rewardDocumentOpen()
        startActivity(
            Intent(this, DetailActivity::class.java)
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
            EventUtil.onEvent(this, EventUtil.addLater)
        }
    }
}
