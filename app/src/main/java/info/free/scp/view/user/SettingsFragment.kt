package info.free.scp.view.user

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import info.free.scp.R
import info.free.scp.util.*


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
        setPreferencesFromResource(R.xml.pref_general_settings, rootKey)
        preferenceManager?.sharedPreferencesName = "general_settings"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager?.sharedPreferences?.edit()?.putBoolean("dark_mode", ThemeUtil.currentTheme == 1)?.apply()
        findPreference("dark_mode").setDefaultValue(ThemeUtil.currentTheme == 1)

        if (!BackupHelper.getInstance(context!!).checkBackUpFileExist()) {
            findPreference("restore_data").isEnabled = false
        }
        findPreference("backup_data").title = "备份数据库${if (BackupHelper.getInstance(context!!)
                        .checkBackUpFileExist()) "（已有旧的备份数据）" else ""}"
        findPreference("restore_data").title = "恢复本地数据库${if (BackupHelper.getInstance(context!!)
                        .checkBackUpFileExist()) "（已有旧的备份数据）" else ""}"

        findPreference("dark_mode")?.setOnPreferenceClickListener {
            ThemeUtil.changeTheme(activity, if (ThemeUtil.currentTheme == 1) 0 else 1)
            true
        }


        findPreference("sync_data")?.setOnPreferenceClickListener {
            if (PreferenceUtil.getInitCategoryFinish()) {
                AlertDialog.Builder(activity)
                        .setTitle("注意")
                        .setMessage("该选项将删除所有目录及正文数据并重新加载，是否确定？")
                        .setPositiveButton("确定") { dialog, _ ->
                            EventUtil.onEvent(activity, EventUtil.clickSyncData)
                            listener?.onResetDataClick()
                            dialog.dismiss()
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            } else {
                EventUtil.onEvent(activity, EventUtil.clickSyncData)
                listener?.onResetDataClick()
                // 只允许点击一次
                it.isEnabled = false
            }
            true
        }

        findPreference("backup_data")?.setOnPreferenceClickListener {
            context?.let {ctx->
                EventUtil.onEvent(ctx, EventUtil.clickBackUpData)
                BackupHelper.getInstance(ctx).backupDB()
                findPreference("restore_data").isEnabled = true
            }
            true
        }
        findPreference("restore_data")?.setOnPreferenceClickListener {
            context?.let {ctx->
                EventUtil.onEvent(ctx, EventUtil.clickRestoreData)
                BackupHelper.getInstance(ctx).restoreDB()
            }
            true
        }
    }
}