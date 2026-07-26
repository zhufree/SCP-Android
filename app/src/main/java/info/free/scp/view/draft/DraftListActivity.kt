package info.free.scp.view.draft

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import info.free.scp.R
import info.free.scp.components.CommonTopBar
import info.free.scp.ui.MainTheme
import info.free.scp.ui.draft.DraftListScreen
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.viewmodel.DraftListViewModel
import info.free.scp.viewmodel.DraftListUiState

class DraftListActivity : BaseActivity() {
    private val model by viewModels<DraftListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by model.uiState.observeAsState(DraftListUiState())
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getString(R.string.draft_list),
                            actions = {
                                IconButton(onClick = { openDraftHelp() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_help_outline_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Help"
                                    )
                                }
                                IconButton(onClick = { startActivity(Intent(this@DraftListActivity, DraftEditActivity::class.java)) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_add_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Add"
                                    )
                                }
                            },
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier.padding(padding)
                    ) {
                        DraftListScreen(
                            drafts = uiState.drafts,
                            onOpenDraft = { draft ->
                                startActivity(
                                    Intent(this@DraftListActivity, DraftEditActivity::class.java)
                                        .putExtra("draftId", draft.draftId)
                                )
                            },
                            onDeleteDraft = { draft ->
                                model.deleteDraft(draft.draftId)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun openDraftHelp() {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("http://scp-wiki-cn.wikidot.com/how-to-write-an-scp")
            }
        )
    }
}
