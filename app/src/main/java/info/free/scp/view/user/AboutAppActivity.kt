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
import info.free.scp.databinding.ActivityAboutAppBinding
import info.free.scp.databinding.LayoutDialogCopyrightBinding
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.home.PrivacyActivity
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

class AboutAppActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutAppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseToolbar = binding.appToolbar

        arrayOf(binding.btnQqGroup, binding.btnQqChannel, binding.btnGoSite).forEach {
            it.background = ThemeUtil.customShape(ThemeUtil.linkBlue, 0, 0, dip(24))
        }
        binding.btnShowCopyright.background =
            ThemeUtil.customShape(ThemeUtil.todoYellow, 0, 0, dip(24))
        binding.btnShowPrivacy.background =
            ThemeUtil.customShape(ThemeUtil.todoYellow, 0, 0, dip(24))

        binding.tvAppNotice.text = "最新公告：${PreferenceUtil.getNotice()}"
        binding.tvVersion.text = "version: ${BuildConfig.VERSION_NAME}"

        binding.btnGoSite.setOnClickListener {
            Utils.openUrl("http://scp-wiki-cn.wikidot.com/")
        }

        binding.btnQqGroup.setOnClickListener {
            Utils.openUrl("https://jq.qq.com/?_wv=1027&k=7wWIlcxZ")
        }

        binding.btnQqChannel.setOnClickListener {
            Utils.openUrl("https://pd.qq.com/s/4tqu31")
        }

        binding.btnShowCopyright.setOnClickListener {
            showCopyright()
        }
        binding.btnShowPrivacy.setOnClickListener {
            startActivity<PrivacyActivity>()
        }
    }

    private fun showCopyright() {
        val binding = LayoutDialogCopyrightBinding.inflate(LayoutInflater.from(this))
        val copyrightView =
            LayoutInflater.from(this).inflate(R.layout.layout_dialog_copyright, null)
        val copySpan1 = SpannableString(getString(R.string.copyright_notice_1))
        val copySpan2 = SpannableString(getString(R.string.copyright_notice_2))
        val copySpan3 = SpannableString(getString(R.string.copyright_notice_3))
        val startIndex1 = copySpan1.indexOf("http")
        val startIndex2 = copySpan2.indexOf("http")
        val startIndex3 = copySpan3.indexOf("http")
        copySpan1.setSpan(
            CopySpan("http://scp-wiki-cn.wikidot.com/", this), startIndex1,
            copySpan1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        copySpan2.setSpan(
            CopySpan("https://creativecommons.org/licenses/by-sa/3.0/deed.zh", this), startIndex2,
            copySpan2.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        copySpan3.setSpan(
            CopySpan("http://scp-wiki-cn.wikidot.com/licensing-guide", this), startIndex3,
            copySpan3.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        binding.tvCopyright1.text = copySpan1
        binding.tvCopyright2.text = copySpan2
        binding.tvCopyright3.text = copySpan3
        binding.tvCopyright1.movementMethod = LinkMovementMethod.getInstance()
        binding.tvCopyright2.movementMethod = LinkMovementMethod.getInstance()
        binding.tvCopyright3.movementMethod = LinkMovementMethod.getInstance()
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
