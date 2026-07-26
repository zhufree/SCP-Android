package info.free.scp.view.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import info.free.scp.R
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import info.free.scp.components.CommonTopBar
import info.free.scp.data.direct.DirectDocumentRepository
import info.free.scp.data.direct.DirectLookupResult
import info.free.scp.ui.MainTheme
import info.free.scp.ui.home.DirectScreen
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
import kotlinx.coroutines.launch

class DirectActivity : BaseActivity() {
    private val directDocumentRepository = DirectDocumentRepository()
    private var chooseType by mutableIntStateOf(0)
    private var cnString by mutableStateOf("")
    private var jString by mutableStateOf("")
    private var numberString by mutableStateOf("")
    private val expression: String
        get() = "SCP-$cnString$numberString$jString"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getString(R.string.title_direct),
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    DirectScreen(
                        expression = expression,
                        onToggleCn = { toggleCn() },
                        onToggleJoke = { toggleJoke() },
                        onBackspace = { backspace() },
                        onNumber = { numberString += it },
                        onGo = { lifecycleScope.launch { goDirect() } },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    private fun toggleCn() {
        chooseType = when (chooseType) {
            0 -> 1
            1 -> 0
            2 -> 3
            3 -> 2
            else -> 1
        }
        updateAffixes()
    }

    private fun toggleJoke() {
        chooseType = when (chooseType) {
            0 -> 2
            1 -> 3
            2 -> 0
            3 -> 1
            else -> 2
        }
        updateAffixes()
    }

    private fun updateAffixes() {
        cnString = when (chooseType) {
            1, 3 -> "CN-"
            else -> ""
        }
        jString = when (chooseType) {
            2, 3 -> "-J"
            else -> ""
        }
    }

    private fun backspace() {
        if (numberString.isNotEmpty()) {
            numberString = numberString.substring(0, numberString.length - 1)
        }
    }

    private suspend fun goDirect() {
        val scpType = when (chooseType) {
            0 -> SAVE_SERIES
            1 -> SAVE_SERIES_CN
            2 -> SAVE_JOKE
            3 -> SAVE_JOKE_CN
            else -> SAVE_SERIES
        }
        val number = when (chooseType) {
            0, 1 -> "%-$numberString %"
            2, 3 -> "%-$numberString-%"
            else -> "%-$numberString"
        }
        when (val result = directDocumentRepository.findDocument(scpType, number)) {
            is DirectLookupResult.Found -> {
                EventUtil.onEvent(this, EventUtil.clickDirect)
                val detailIntent = Intent(this, DetailActivity::class.java)
                    .putExtra("link", result.document.link)
                if (result.includeTitleInNavigation) {
                    detailIntent.putExtra("title", result.document.title)
                }
                startActivity(detailIntent)
            }
            DirectLookupResult.NotFound -> showNotFound()
            DirectLookupResult.Failed -> Unit
        }
    }

    private fun showNotFound() {
        Toast.makeText(this, "\u6ca1\u6709\u8fd9\u7bc7\u6587\u7ae0", Toast.LENGTH_SHORT).show()
    }
}
