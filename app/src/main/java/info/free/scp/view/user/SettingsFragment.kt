package info.free.scp.view.user

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.util.*
import info.free.scp.util.EventUtil.clickCopyright
import info.free.scp.util.EventUtil.clickDownloadSetting
import info.free.scp.util.EventUtil.clickReadSetting
import kotlinx.android.synthetic.main.layout_dialog_copyright.view.*


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
            EventUtil.onEvent(activity, clickReadSetting)
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.putExtra("setting_type", 0)
            activity?.startActivity(intent)
            true
        }
        findPreference("download_settings").setOnPreferenceClickListener {
            EventUtil.onEvent(activity, clickDownloadSetting)
            val intent = Intent(activity, SettingsActivity::class.java)
            intent.putExtra("setting_type", 1)
            activity?.startActivity(intent)
            true
        }
        findPreference("copyright").setOnPreferenceClickListener {
            EventUtil.onEvent(activity, clickCopyright)
            val copyrightView = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_copyright, null)
            val copySpan1 = SpannableString(getString(R.string.copyright_notice_1))
            val copySpan2 = SpannableString(getString(R.string.copyright_notice_2))
            val copySpan3 = SpannableString(getString(R.string.copyright_notice_3))
            val startIndex1 = copySpan1.indexOf("http")
            val startIndex2 = copySpan2.indexOf("http")
            val startIndex3 = copySpan3.indexOf("http")
            copySpan1.setSpan(CopySpan("http://scp-wiki-cn.wikidot.com/", activity), startIndex1,
                    copySpan1.length, SPAN_INCLUSIVE_EXCLUSIVE)
            copySpan2.setSpan(CopySpan("https://creativecommons.org/licenses/by-sa/3.0/deed.zh", activity), startIndex2,
                    copySpan2.length, SPAN_INCLUSIVE_EXCLUSIVE)
            copySpan3.setSpan(CopySpan("http://scp-wiki-cn.wikidot.com/licensing-guide", activity), startIndex3,
                    copySpan3.length, SPAN_INCLUSIVE_EXCLUSIVE)
            copyrightView.tv_copyright_1.text = copySpan1
            copyrightView.tv_copyright_2.text = copySpan2
            copyrightView.tv_copyright_3.text = copySpan3
            copyrightView.tv_copyright_1.movementMethod = LinkMovementMethod.getInstance()
            copyrightView.tv_copyright_2.movementMethod = LinkMovementMethod.getInstance()
            copyrightView.tv_copyright_3.movementMethod = LinkMovementMethod.getInstance()
            val copyrightDialog = AlertDialog.Builder(activity)
                    .setTitle("版权说明")
                    .setView(copyrightView) // 设置显示的view
                    .setPositiveButton("OK") { _, _ -> }
                    .create()
// 因为后面要通过dialog获取button，此时要单独获取dialog对象，然后手动show()
            copyrightDialog.show()
// 获取button并设置点击事件
            copyrightDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                copyrightDialog.dismiss()
            }
            true
        }
    }

    class CopySpan(val url: String, val activity: FragmentActivity?) : ClickableSpan() {
        override fun onClick(widget: View) {
            val copyrightIntent = Intent()
            copyrightIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse(url)
            copyrightIntent.data = updateUrl
            activity?.startActivity(copyrightIntent)
        }
    }
}