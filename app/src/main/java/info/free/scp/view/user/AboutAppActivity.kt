package info.free.scp.view.user

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.home.PrivacyActivity
import kotlinx.android.synthetic.main.activity_about_app.*
import kotlinx.android.synthetic.main.layout_dialog_copyright.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AboutAppActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickAboutApp)
        setContentView(R.layout.activity_about_app)

        baseToolbar = app_toolbar

        arrayOf(btn_qq_group, btn_go_site).forEach {
            it.background = ThemeUtil.customShape(ThemeUtil.linkBlue, 0, 0, dip(24))
        }
        btn_show_copyright?.background = ThemeUtil.customShape(ThemeUtil.todoYellow, 0, 0, dip(24))
        btn_show_privacy?.background = ThemeUtil.customShape(ThemeUtil.todoYellow, 0, 0, dip(24))

        tv_app_notice?.text = "最新公告：${PreferenceUtil.getNotice()}"
        tv_version?.text = "version: ${BuildConfig.VERSION_NAME}"
//        tv_server_data_latest_time?.text = "云端最新数据更新时间：${PreferenceUtil.getServerLastUpdateTime()}"

        btn_go_site?.setOnClickListener {
            val intent = Utils.getUrlIntent("http://scp-wiki-cn.wikidot.com/")
            startActivity(intent)
        }

        btn_show_copyright?.setOnClickListener {
            showCopyright()
        }
        btn_show_privacy?.setOnClickListener {
            startActivity<PrivacyActivity>()
        }
    }

    private fun showCopyright() {
        val copyrightView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_copyright, null)
        val copySpan1 = SpannableString(getString(R.string.copyright_notice_1))
        val copySpan2 = SpannableString(getString(R.string.copyright_notice_2))
        val copySpan3 = SpannableString(getString(R.string.copyright_notice_3))
        val startIndex1 = copySpan1.indexOf("http")
        val startIndex2 = copySpan2.indexOf("http")
        val startIndex3 = copySpan3.indexOf("http")
        copySpan1.setSpan(CopySpan("http://scp-wiki-cn.wikidot.com/", this), startIndex1,
                copySpan1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        copySpan2.setSpan(CopySpan("https://creativecommons.org/licenses/by-sa/3.0/deed.zh", this), startIndex2,
                copySpan2.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        copySpan3.setSpan(CopySpan("http://scp-wiki-cn.wikidot.com/licensing-guide", this), startIndex3,
                copySpan3.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        copyrightView.tv_copyright_1.text = copySpan1
        copyrightView.tv_copyright_2.text = copySpan2
        copyrightView.tv_copyright_3.text = copySpan3
        copyrightView.tv_copyright_1.movementMethod = LinkMovementMethod.getInstance()
        copyrightView.tv_copyright_2.movementMethod = LinkMovementMethod.getInstance()
        copyrightView.tv_copyright_3.movementMethod = LinkMovementMethod.getInstance()
        val copyrightDialog = AlertDialog.Builder(this)
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
    }

    class CopySpan(val url: String, val activity: androidx.fragment.app.FragmentActivity?) : ClickableSpan() {
        override fun onClick(widget: View) {
            val copyrightIntent = Intent()
            copyrightIntent.action = "android.intent.action.VIEW"
            val updateUrl = Uri.parse(url)
            copyrightIntent.data = updateUrl
            activity?.startActivity(copyrightIntent)
        }
    }
}
