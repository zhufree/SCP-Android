package info.free.scp.view.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.bean.DownloadModel
import info.free.scp.databinding.ActivityDownloadBinding
import info.free.scp.db.ScpDatabase
import info.free.scp.util.FileUtil
import info.free.scp.util.InjectorUtils
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class DownloadActivity : BaseActivity() {

    private var downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val completeDownloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    ?: -1
            if (completeDownloadId > 0) {
                val pfd = ScpApplication.downloadManager.openDownloadedFile(completeDownloadId)
                doAsync {
                    try {
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
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast("文件复制出错：" + e.message)
                    }

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bd: ActivityDownloadBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_download)

        setSupportActionBar(bd.downloadToolbar)
        bd.downloadToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        bd.downloadToolbar.setNavigationOnClickListener { finish() }

        val adapter = DownloadAdapter()
        bd.rlDownloadList.adapter = adapter
        val factory = InjectorUtils.provideDownloadListViewModelFactory()
        val model = ViewModelProviders.of(this, factory)
                .get(DownloadListViewModel::class.java)


        model.getDownloadList()
                ?.observe(this, Observer<List<DownloadModel>> { downloads ->
                    // update UI
                    if (downloads.isNotEmpty()) {
                        adapter.submitList(downloads)
                    }
                })
        registerReceiver(downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }
}
