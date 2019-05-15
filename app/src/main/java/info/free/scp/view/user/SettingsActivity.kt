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
        val settingType = intent.getIntExtra("setting_type", 0)
        if (settingType == 1) {
            settings_toolbar?.inflateMenu(R.menu.download_setting_menu)
            settings_toolbar?.setOnMenuItemClickListener {
                if (it.itemId == R.id.download_help) {
                    AlertDialog.Builder(this)
                            .setTitle("离线说明")
                            .setMessage(R.string.download_explain)
                            .setPositiveButton("OK") { _, _ -> }
                            .create().show()
                }
                true
            }
        }
        supportFragmentManager.beginTransaction().replace(R.id.fl_read_settings,
            ReadSettingsFragment()).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.download_setting_menu, menu)
        return true
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
            findPreference<SwitchPreference>("hide_finished_article")?.setOnPreferenceClickListener {
                EventUtil.onEvent(context, EventUtil.hideReadContent)
                true
            }
            findPreference<DialogPreference>("category_count")?.setOnPreferenceChangeListener { _, any ->
                EventUtil.onEvent(context, EventUtil.setListItemCount, any.toString())
                true
            }
        }
    }
}
