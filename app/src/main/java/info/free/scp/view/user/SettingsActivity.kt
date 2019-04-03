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
import info.free.scp.db.ScpDao
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
        supportFragmentManager.beginTransaction().replace(R.id.fl_read_settings, if (settingType == 0)
            ReadSettingsFragment() else DownloadFragment()).commit()
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
    class DownloadFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_download_settings, rootKey)
            preferenceManager?.sharedPreferencesName = "download_settings"
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            findPreference<Preference>("download_scp")?.summary = getString(R.string.download_summary,
                    PreferenceUtil.getServerLastUpdateTime(0),
                    PreferenceUtil.getDetailLastLoadTime(0))
            findPreference<Preference>("download_scp_cn")?.summary = getString(R.string.download_summary,
                    PreferenceUtil.getServerLastUpdateTime(1),
                    PreferenceUtil.getDetailLastLoadTime(1))
            findPreference<Preference>("download_tale")?.summary = getString(R.string.download_summary,
                    PreferenceUtil.getServerLastUpdateTime(2),
                    PreferenceUtil.getDetailLastLoadTime(2))
            findPreference<Preference>("download_other")?.summary = getString(R.string.download_summary,
                    PreferenceUtil.getServerLastUpdateTime(3),
                    PreferenceUtil.getDetailLastLoadTime(3))
            findPreference<Preference>("download_collection")?.summary = getString(R.string.download_summary,
                    PreferenceUtil.getServerLastUpdateTime(4),
                    PreferenceUtil.getDetailLastLoadTime(4))
            findPreference<Preference>("download_scp")?.setOnPreferenceClickListener {
                showNoticeDialog(DOWNLOAD_SCP)
                true
            }
            findPreference<Preference>("download_scp_cn")?.setOnPreferenceClickListener {
                showNoticeDialog(DOWNLOAD_SCP_CN)
                true
            }
            findPreference<Preference>("download_tale")?.setOnPreferenceClickListener {
                showNoticeDialog(DOWNLOAD_TALE)
                true
            }
            findPreference<Preference>("download_other")?.setOnPreferenceClickListener {
                showNoticeDialog(DOWNLOAD_ARCHIVES)
                true
            }
            findPreference<Preference>("download_collection")?.setOnPreferenceClickListener {
                showNoticeDialog(DOWNLOAD_COLLECTIONS)
                true
            }
            findPreference<Preference>("sync_category")?.setOnPreferenceClickListener {
                ScpDao.getInstance().resetCategoryData()
                UpdateManager.getInstance(activity as BaseActivity).initCategoryData()
                true
            }

            if (!BackupHelper.getInstance(context!!).checkBackUpFileExist()) {
                findPreference<Preference>("restore_data")?.isEnabled = false
            }
            findPreference<Preference>("backup_data")?.title = "备份数据库${if (BackupHelper.getInstance(context!!)
                            .checkBackUpFileExist()) "（已有旧的备份数据）" else ""}"
            findPreference<Preference>("restore_data")?.title = "恢复本地数据库${if (BackupHelper.getInstance(context!!)
                            .checkBackUpFileExist()) "（已有旧的备份数据）" else ""}"

            findPreference<Preference>("backup_data")?.setOnPreferenceClickListener {
                context?.let {ctx->
                    BackupHelper.getInstance(ctx).backupDB()
                    findPreference<Preference>("restore_data")?.isEnabled = true
                }
                true
            }
            findPreference<Preference>("restore_data")?.setOnPreferenceClickListener {
                context?.let {ctx->
                    BackupHelper.getInstance(ctx).restoreDB()
                }
                true
            }

            findPreference<Preference>("sync_data")?.setOnPreferenceClickListener {
                if (PreferenceUtil.getInitCategoryFinish()) {
                    AlertDialog.Builder(activity)
                            .setTitle("注意")
                            .setMessage("该选项将删除所有目录及正文数据并重新加载，是否确定？")
                            .setPositiveButton("确定") { dialog, _ ->
                                EventUtil.onEvent(activity, EventUtil.clickSyncData)
                                UpdateManager.getInstance(activity as BaseActivity).checkInitData(true)
                                dialog.dismiss()
                            }
                            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                            .create().show()
                } else {
                    EventUtil.onEvent(activity, EventUtil.clickSyncData)
                    UpdateManager.getInstance(activity as BaseActivity).checkInitData(true)
                }
                true
            }
        }

        private fun showNoticeDialog(downloadType: Int) {
            if (PreferenceUtil.getDetailDataLoadFinish(downloadType)) {
                AlertDialog.Builder(activity)
                        .setTitle("同步${Utils.getDownloadTitleByType(downloadType)}")
                        .setMessage("删除该部分本地数据并从云端重新下载最新数据？")
                        .setPositiveButton("确定") { _, _ ->
                            PreferenceUtil.setDetailDataLoadFinish(downloadType, false)
                            PreferenceUtil.setSingleDbLoadCount(downloadType, 0)
                            ScpDao.getInstance().deleteDetailByDownloadType(downloadType)
                            UpdateManager.getInstance(activity as BaseActivity).initDetailData(downloadType)
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            } else {
                AlertDialog.Builder(activity)
                        .setTitle("Notice")
                        .setMessage("当前离线未完成，请等待完成才可以选择更新（如果判断错误请选择同步云端数据重新离线）")
                        .setPositiveButton("确定") { _, _ -> }
                        .create().show()
            }
        }
    }
}
