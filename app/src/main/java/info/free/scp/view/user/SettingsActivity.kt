package info.free.scp.view.user

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import info.free.scp.R
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
        supportFragmentManager.beginTransaction().replace(R.id.fl_read_settings, ReadSettingsFragment()).commit()
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class ReadSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_read_settings, rootKey)
            preferenceManager?.sharedPreferencesName = "read_settings"
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            findPreference("hide_finished_article").setOnPreferenceClickListener {
                // TODO event
                true
            }
        }
    }


}
