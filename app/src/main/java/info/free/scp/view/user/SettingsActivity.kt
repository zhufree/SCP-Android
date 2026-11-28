package info.free.scp.view.user

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.components.CommonTopBar
import info.free.scp.data.settings.DetailSettingsStore
import info.free.scp.data.settings.PreferenceDetailSettingsStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.data.settings.ReadingPreferencesStore
import info.free.scp.data.settings.storedToTextSize
import info.free.scp.data.settings.textSizeToStored
import info.free.scp.ui.MainTheme
import info.free.scp.ui.settings.SettingsScreen
import info.free.scp.util.EventUtil
import info.free.scp.util.UpdateManager
import info.free.scp.view.base.BaseActivity
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    private val updates get() = ScpApplication.dataUpdates
    private val readingPreferences: ReadingPreferencesStore = PreferenceSettingsStore()
    private val detailSettings: DetailSettingsStore = PreferenceDetailSettingsStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                try {
                    updates.state.map { it.transfer != null }.distinctUntilChanged().collect { active ->
                        if (active) window.addFlags(flag) else window.clearFlags(flag)
                    }
                } finally {
                    window.clearFlags(flag)
                }
            }
        }
        setContent {
            MainTheme {
                val updateState by updates.state.collectAsState()
                var hideFinished by remember { mutableStateOf(readingPreferences.hideFinishedArticles) }
                var textSize by remember { mutableIntStateOf(storedToTextSize(detailSettings.detailTextSize)) }
                var hanzType by remember { mutableIntStateOf(readingPreferences.hanzType) }
                var wifiOnly by remember { mutableStateOf(updates.wifiOnly) }
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getString(R.string.title_activity_settings),
                            onBackPressed = { finish() }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { padding ->
                    SettingsScreen(
                        modifier = Modifier.padding(padding),
                        hideFinished = hideFinished,
                        onHideFinishedChange = {
                            hideFinished = it
                            readingPreferences.hideFinishedArticles = it
                            EventUtil.onEvent(this, EventUtil.hideReadContent)
                        },
                        textSize = textSize,
                        onTextSizeChange = { textSize = it },
                        onTextSizeChangeFinished = { detailSettings.detailTextSize = textSizeToStored(it) },
                        hanzType = hanzType,
                        onHanzTypeChange = {
                            hanzType = it
                            readingPreferences.hanzType = it
                        },
                        updateState = updateState,
                        onCheckUpdate = ::checkUpdates,
                        wifiOnly = wifiOnly,
                        onWifiOnlyChange = {
                            wifiOnly = it
                            updates.wifiOnly = it
                        },
                        onInstallPart = { updates.installPart(it) },
                        onInstallAllParts = { updates.installAllParts() },
                        onCancelDownload = { updates.cancelDownload() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 每次回到前台重读本地安装状态，和旧版 repeatOnLifecycle(STARTED) 里的 refresh 一致
        updates.refresh()
    }

    private fun checkUpdates() {
        if (updates.canConfirmCachedCatalog()) {
            UpdateManager.confirmCatalogDownload(this)
        } else {
            updates.check(manual = true)
        }
    }
}
