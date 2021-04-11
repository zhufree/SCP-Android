package info.free.scp.view.user

import android.os.Bundle
import info.free.scp.R
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_download.*
import org.jetbrains.anko.*


class DownloadActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        baseToolbar = download_toolbar

        tv_go_download.setOnClickListener {
            val intent = Utils.getUrlIntent("https://mianbaoduo.com/o/bread/YZiZl51u")
            startActivity(intent)
        }

        tv_go_download?.post {
            tv_go_download?.background = ThemeUtil.customShape(ThemeUtil.linkBlue,
                    0, 0, dip(24))
        }
        btn_backup?.post {
            btn_backup?.background = ThemeUtil.customShape(ThemeUtil.linkBlue,
                    0, 0, dip(24))
        }

        btn_backup?.setOnClickListener {
            toast("开始备份")
            doAsync {
                FileUtil.getInstance(this@DownloadActivity).backup()
                uiThread {
                    toast("备份完成")
                }
            }
        }
    }
}
