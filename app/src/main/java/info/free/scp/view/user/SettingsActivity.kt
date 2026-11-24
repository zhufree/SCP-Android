package info.free.scp.view.user

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.components.CommonTopBar
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.data.settings.ReadingPreferencesStore
import info.free.scp.data.update.CatalogVersion
import info.free.scp.data.update.DataUpdateState
import info.free.scp.data.update.MIB
import info.free.scp.data.update.PartAction
import info.free.scp.ui.MainTheme
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.UpdateManager
import info.free.scp.view.base.BaseActivity
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getString(R.string.title_activity_settings),
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        factory = { context ->
                            FrameLayout(context).apply {
                                id = R.id.fl_read_settings
                            }
                        }
                    )
                    LaunchedEffect(Unit) {
                        if (supportFragmentManager.findFragmentById(R.id.fl_read_settings) == null) {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fl_read_settings, ReadSettingsFragment())
                                .commit()
                        }
                    }
                }
            }
        }
    }

    class ReadSettingsFragment : PreferenceFragmentCompat() {
        private val updates get() = ScpApplication.dataUpdates
        private val readingPreferences: ReadingPreferencesStore = PreferenceSettingsStore()
        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_read_settings, rootKey)
            preferenceManager?.sharedPreferencesName = "read_settings"
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            findPreference<SwitchPreference>("hide_finished_article")?.isChecked =
                readingPreferences.hideFinishedArticles
            findPreference<SwitchPreference>("hide_finished_article")?.setOnPreferenceClickListener {
                EventUtil.onEvent(context, EventUtil.hideReadContent)
                false
            }

            findPreference<Preference>("check_data_update")?.setOnPreferenceClickListener {
                if (updates.canConfirmCachedCatalog()) {
                    UpdateManager.confirmCatalogDownload(requireActivity() as BaseActivity)
                } else {
                    updates.check(manual = true)
                }
                true
            }
            findPreference<SwitchPreference>("detail_wifi_only")?.apply {
                isChecked = updates.wifiOnly
                setOnPreferenceChangeListener { _, value ->
                    updates.wifiOnly = value as Boolean
                    true
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    updates.refresh()
                    updates.state.collect(::renderUpdates)
                }
            }
        }

        private fun renderUpdates(state: DataUpdateState) {
            findPreference<Preference>("check_data_update")?.apply {
                val installed = when (state.catalog) {
                    CatalogVersion.Legacy -> "目录：内置旧版"
                    null -> "目录版本未知"
                    else -> "目录：${state.catalog.exportId} · ${state.catalog.exportedAt.take(10)}"
                }
                val pending = state.stagedCatalog?.let {
                    "待安装：${it.exportId} · ${it.exportedAt.take(10)}（下次冷启动）"
                }
                summary = listOfNotNull(installed, pending, state.status.takeIf { it.isNotEmpty() }).joinToString("\n")
                isEnabled = !state.busy
            }
            val section = findPreference<PreferenceCategory>("detail_parts") ?: return
            val note = findPreference<Preference>("detail_parts_note") ?: return
            note.summary = listOfNotNull(
                "内容来自 SCP 基金会中文站（CC BY-SA 3.0）",
                "无法读取本地正文库".takeIf { state.detailInventoryUnavailable },
                "整库导入（未分块）".takeIf { state.fullDetail },
                "正文整库将在下次冷启动时生效".takeIf { state.legacyDetailPending },
                "分块规则已更新，请重装已有正文块".takeIf { state.reinstallParts && !state.obsoletePartsManifest },
                "清单分块规则早于本地版本，请等待后续更新".takeIf { state.obsoletePartsManifest },
                "检查数据更新后可查看可下载的正文块".takeIf { state.parts.isEmpty() }
            ).joinToString("\n")
            // Preserve preference identity and focus while download progress changes.
            val partKeys = state.parts.mapTo(mutableSetOf()) { "detail_part_${it.id}" }
            for (index in section.preferenceCount - 1 downTo 0) {
                val row = section.getPreference(index)
                if (row.key?.startsWith("detail_part_") == true && row.key !in partKeys) section.removePreference(row)
            }
            state.parts.forEachIndexed { index, part ->
                val key = "detail_part_${part.id}"
                val row = section.findPreference<Preference>(key) ?: Preference(requireContext()).apply {
                    this.key = key
                    section.addPreference(this)
                }
                val action = state.partActions[part.id]
                val label = when (action) {
                    null -> "安装状态未知"
                    PartAction.SKIP -> "已装"
                    PartAction.REPLACE -> "有更新 · 点击更新"
                    else -> "未装 · 点击安装"
                }
                row.order = index + 1
                row.title = part.label
                row.summary = getString(R.string.detail_part_summary, part.file.size.toDouble() / MIB, label)
                row.isEnabled = !state.busy && !state.legacyDetailPending &&
                    !state.detailInventoryUnavailable && !state.obsoletePartsManifest && action != PartAction.SKIP
                row.setOnPreferenceClickListener {
                    updates.installPart(part.id)
                    true
                }
            }
        }
    }
}
