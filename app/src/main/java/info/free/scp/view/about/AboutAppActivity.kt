package info.free.scp.view.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about_app.*

class AboutAppActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        tv_version?.text = "version: ${BuildConfig.VERSION_NAME}"

        tv_qq_group?.setOnLongClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText("qqGroup", "805194504")
            clipboardManager?.primaryClip = clipData
            Toaster.show("已复制到剪贴板")
            true
        }
    }
}
