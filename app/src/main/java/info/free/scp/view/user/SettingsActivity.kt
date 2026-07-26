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
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import info.free.scp.R
import info.free.scp.components.CommonTopBar
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.data.settings.ReadingPreferencesStore
import info.free.scp.ui.MainTheme
import info.free.scp.util.EventUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity

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
        }
    }
}
