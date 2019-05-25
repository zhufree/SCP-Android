package info.free.scp.view.user

import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceActivity
import androidx.preference.PreferenceFragmentCompat
import android.view.Menu
import android.view.View
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import info.free.scp.R
import info.free.scp.SCPConstants.Download.DOWNLOAD_ARCHIVES
import info.free.scp.SCPConstants.Download.DOWNLOAD_COLLECTIONS
import info.free.scp.SCPConstants.Download.DOWNLOAD_SCP
import info.free.scp.SCPConstants.Download.DOWNLOAD_SCP_CN
import info.free.scp.SCPConstants.Download.DOWNLOAD_TALE
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settings_toolbar)
        settings_toolbar?.setTitle(R.string.app_name)
        settings_toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        settings_toolbar?.setNavigationOnClickListener { finish() }

        supportFragmentManager.beginTransaction().replace(R.id.fl_read_settings,
                ReadSettingsFragment()).commit()
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class ReadSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_read_settings, rootKey)
            preferenceManager?.sharedPreferencesName = "read_settings"
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            findPreference<SwitchPreference>("hide_finished_article")?.isChecked =
                    PreferenceUtil.getHideFinished()
            findPreference<SwitchPreference>("hide_finished_article")?.setOnPreferenceClickListener {
                EventUtil.onEvent(context, EventUtil.hideReadContent)
                false
            }
            findPreference<DialogPreference>("category_count")?.setOnPreferenceChangeListener { _, any ->
                EventUtil.onEvent(context, EventUtil.setListItemCount, any.toString())
                true
            }
        }
    }
}
