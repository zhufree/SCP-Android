package info.free.scp.view.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.db.ScpDatabase
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_download.*
import org.jetbrains.anko.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class DownloadActivity : BaseActivity() {

    private var downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val completeDownloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    ?: -1
            if (completeDownloadId > 0) {
                doAsync {
                    try {
                        val pfd = ScpApplication.downloadManager.openDownloadedFile(completeDownloadId)
                        val fileInputStream = FileInputStream(pfd.fileDescriptor)
                        val destFile = File(FileUtil.privateDbDirPath + FileUtil.dataDbFilename)
                        val outputStream = FileOutputStream(destFile)
                        destFile.outputStream().use { output ->
                            fileInputStream.copyTo(output)
                        }
                        outputStream.flush()
                        outputStream.close()
                        fileInputStream.close()
                        ScpDatabase.getNewInstance()
                        uiThread {
                            toast("复制完成")
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast("文件复制出错：" + e.message)
                    }
                }
            }
        }
    }
    val downloadUrl = PreferenceUtil.getDownloadLink()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        baseToolbar = download_toolbar

        tv_download_time.text = "本地同步时间：${Utils.formatDate(PreferenceUtil.getDetailLastLoadTime())}"
        tv_update_time.text = "服务器更新时间：${Utils.formatDate(PreferenceUtil.getServerLastUpdateTime())}"
        btn_download?.post {
            btn_download?.background = ThemeUtil.customShape(resources.getColor(R.color.colorPrimaryDark),
                    0, 0, dip(30))
        }
        btn_download?.setOnClickListener {
            downloadData()
        }
        registerReceiver(downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        if (downloadUrl.isEmpty()) {
            toast("下载链接未加载完成")
        }
    }

    var downloadId = -1L
    var mStartVideoHandler: Handler = Handler()
    private var runnable: Runnable? = null

    private fun downloadData() {

        val fileHelper = FileUtil.getInstance(ScpApplication.context)
        // 检查本地是否有已经下载过的
        if (fileHelper.checkBackupDataExist()) {
            ScpApplication.currentActivity?.alert("检测到该数据库之前已下载完成，是否恢复？", "恢复") {
                positiveButton("恢复") {
                    ScpApplication.context.toast("开始恢复")
                    doAsync {
                        if (fileHelper.restoreData()) {
                            ScpDatabase.getNewInstance()
                            uiThread {
                                ScpApplication.currentActivity?.toast("恢复完成")
                                finish()
                            }
                        }
                    }
                }
                negativeButton("重新下载") {
                    toggleDownloadStatus()
                }
                neutralPressed("取消") {}
            }?.show()
        } else {
            toggleDownloadStatus()
        }
    }

    private fun toggleDownloadStatus() {
        if (downloadId < 0) {
            runnable = Runnable {
                val info: IntArray = DownloadUtil.getDownloadInfo(downloadId)
                tv_download_progress.text = "${info[0] / 1000000}M/${info[1] / 1000000}M ${getStatusByCode(info[2])}"
                runnable?.let {
                    mStartVideoHandler.postDelayed(runnable, 1000)
                }
            }
            runnable?.run()
            downloadId = DownloadUtil.createDownload(downloadUrl)
        } else {
            ScpApplication.downloadManager.remove(downloadId)
            downloadId = -1
            tv_download_progress.text = "${0 / 1000000}M/${0 / 1000000}M ${getStatusByCode(-1)}"
        }
    }


    private fun getStatusByCode(code: Int): String {
        return when (code) {
            1 shl 0 -> {
                "PENDING"
            }
            1 shl 1 -> {
                "下载中"
            }
            1 shl 2 -> {
                "暂停"
            }
            1 shl 3 -> {
                PreferenceUtil.setDetailLastLoadTime(System.currentTimeMillis())
                "下载成功"
            }
            1 shl 4 -> {
                "下载失败"
            }
            -1 -> {
                "取消下载"
            }
            else -> {
                "PENDING"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
        runnable = null
    }
}
