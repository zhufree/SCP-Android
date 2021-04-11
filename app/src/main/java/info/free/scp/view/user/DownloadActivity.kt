package info.free.scp.view.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.RequestCode.REQUEST_PUBLIC_FILE
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_download.*
import org.jetbrains.anko.*
import java.io.File


class DownloadActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        baseToolbar = download_toolbar

        tv_go_download.setOnClickListener {
            val intent = Utils.getUrlIntent("https://mianbaoduo.com/o/bread/YZiZl51u")
            startActivity(intent)
        }

        btn_select_file?.post {
            btn_select_file?.background = ThemeUtil.customShape(ThemeUtil.linkBlue,
                    0, 0, dip(24))
        }
        btn_backup?.post {
            btn_backup?.background = ThemeUtil.customShape(ThemeUtil.linkBlue,
                    0, 0, dip(24))
        }

        btn_select_file?.setOnClickListener {
            requestReadFileTree()
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

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)
        if (reqCode == REQUEST_PUBLIC_FILE) {
            var uri: Uri? = null
            if (data != null) {
                uri = data.data
                val destFilePath = FileUtil.privateDbDirPath + SCPConstants.DETAIL_DB_NAME
                val destFile = File(destFilePath)
                uri?.let {
                    doAsync {
                        if (copyFileFromUri(this@DownloadActivity, uri, destFile)) {
                            uiThread {
                                toast("数据库已加载")
                            }
                        } else {
                            uiThread {
                                toast("数据库读取出错")
                            }
                        }
                    }
                }
            }
        }
    }
}
