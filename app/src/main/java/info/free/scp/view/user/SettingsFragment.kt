package info.free.scp.view.user

import android.content.Intent
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import info.free.scp.R
import info.free.scp.util.*


class SettingsFragment : PreferenceFragmentCompat() {



    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.pref_general_settings, rootKey)
        preferenceManager?.sharedPreferencesName = "general_settings"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager?.sharedPreferences?.edit()?.putBoolean("dark_mode", ThemeUtil.currentTheme == 1)?.apply()
        findPreference("dark_mode").setDefaultValue(ThemeUtil.currentTheme == 1)



        findPreference("dark_mode")?.setOnPreferenceClickListener {
            ThemeUtil.changeTheme(activity, if (ThemeUtil.currentTheme == 1) 0 else 1)
            true
        }

        findPreference("read_settings").setOnPreferenceClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.putExtra("setting_type", 0)
            activity?.startActivity(intent)
            true
        }
        findPreference("download_settings").setOnPreferenceClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.putExtra("setting_type", 1)
            activity?.startActivity(intent)
            true
        }
    }
}