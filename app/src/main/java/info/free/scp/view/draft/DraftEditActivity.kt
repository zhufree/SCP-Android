package info.free.scp.view.draft

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import info.free.scp.R
import info.free.scp.bean.DraftModel
import info.free.scp.components.CommonTopBar
import info.free.scp.data.draft.DraftRepository
import info.free.scp.data.settings.DraftPreferencesStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.ui.MainTheme
import info.free.scp.ui.draft.DraftEditScreen
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity

class DraftEditActivity : BaseActivity() {
    private lateinit var draft: DraftModel
    private val draftRepository = DraftRepository()
    private val draftPreferences: DraftPreferencesStore = PreferenceSettingsStore()
    private var draftTitle by mutableStateOf("")
    private var draftContent by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDraft()
        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getString(R.string.draft_list),
                            actions = {
                                IconButton(onClick = { saveDraft(showMessage = true) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_save_white_24dp),
                                        tint = Color(ThemeUtil.lightText),
                                        contentDescription = "Save"
                                    )
                                }
                            },
                            onBackPressed = {
                                saveDraft(showMessage = false)
                                finish()
                            }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    DraftEditScreen(
                        title = draftTitle,
                        content = draftContent,
                        onTitleChange = {
                            draftTitle = it
                            draft.title = it
                        },
                        onContentChange = {
                            draftContent = it
                            draft.content = it
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::draft.isInitialized) {
            saveDraft(showMessage = false)
        }
    }

    private fun loadDraft() {
        val draftId = intent.getIntExtra("draftId", -1)
        val defaults = draftPreferences.consumeDraftDefaults()
        draft = draftRepository.loadDraft(
            draftId,
            defaults.title,
            defaults.content
        )
        draftTitle = draft.title
        draftContent = draft.content
    }

    private fun saveDraft(showMessage: Boolean) {
        draftRepository.saveDraft(draft, draftTitle, draftContent)
        if (showMessage) {
            Toast.makeText(this, "\u5df2\u4fdd\u5b58", Toast.LENGTH_SHORT).show()
        }
    }
}
