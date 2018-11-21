package info.free.scp.view.user

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import android.view.View
import info.free.scp.R
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil


class SettingsFragment : PreferenceFragmentCompat() {
    private var listener: UserFragment.AboutListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is UserFragment.AboutListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
        preferenceManager?.sharedPreferencesName = "settings"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("theme", PreferenceUtil.getCurrentTheme().toString())
        preferenceManager?.sharedPreferences?.edit()?.putBoolean("dark_mode", ThemeUtil.currentTheme == 1)?.apply()
        findPreference("dark_mode").setOnPreferenceClickListener {
            EventUtil.onEvent(activity, EventUtil.clickChangeTheme)
            ThemeUtil.changeTheme(activity, if (ThemeUtil.currentTheme == 1) 0 else 1)
            true
        }
        findPreference("hide_finished_article").setOnPreferenceClickListener {
            // TODO event
            true
        }

        findPreference("sync_data").setOnPreferenceClickListener {
            if (PreferenceUtil.getInitCategoryFinish()) {
                AlertDialog.Builder(activity)
                        .setTitle("注意")
                        .setMessage("该选项将删除所有目录及正文数据并重新加载，是否确定？")
                        .setPositiveButton("确定") { dialog, _ ->
                            EventUtil.onEvent(activity, EventUtil.clickResetData)
                            listener?.onResetDataClick()
                            // 只允许点击一次
                            it.isEnabled = false
                            dialog.dismiss()
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            } else {
                EventUtil.onEvent(activity, EventUtil.clickResetData)
                listener?.onResetDataClick()
                // 只允许点击一次
                it.isEnabled = false
            }
            true
        }
    }
}