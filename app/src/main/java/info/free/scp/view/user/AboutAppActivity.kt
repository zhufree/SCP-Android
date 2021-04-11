package info.free.scp.view.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about_app.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast

class AboutAppActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventUtil.onEvent(this, EventUtil.clickAboutApp)
        setContentView(R.layout.activity_about_app)

        baseToolbar = app_toolbar

        arrayOf(btn_qq_group, btn_go_site).forEach {
            it.post {
                it.background = ThemeUtil.customShape(ThemeUtil.linkBlue, 0, 0, dip(24))
            }
        }

        tv_app_notice?.text = PreferenceUtil.getNotice()
        tv_version?.text = "version: ${BuildConfig.VERSION_NAME}"
//        tv_server_data_latest_time?.text = "云端最新数据更新时间：${PreferenceUtil.getServerLastUpdateTime()}"

        btn_qq_group?.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText("qqGroup", "805194504")
            clipboardManager?.setPrimaryClip(clipData)
            toast("已复制到剪贴板")
        }
        btn_go_site?.setOnClickListener {
            val intent = Utils.getUrlIntent("http://scp-wiki-cn.wikidot.com/")
            startActivity(intent)
        }
    }
}
