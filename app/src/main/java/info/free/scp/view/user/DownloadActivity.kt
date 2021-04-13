package info.free.scp.view.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.PREF_NAME
import info.free.scp.SCPConstants.RequestCode.REQUEST_BACKUP_DIR
import info.free.scp.SCPConstants.RequestCode.REQUEST_PUBLIC_FILE
import info.free.scp.SCPConstants.RequestCode.REQUEST_RESTORE_DIR
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
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

        arrayOf(btn_select_file, btn_backup, btn_restore).forEach {
            it?.background = ThemeUtil.customShape(ThemeUtil.linkBlue,
                    0, 0, dip(24))
        }

        btn_select_file?.setOnClickListener {
            requestReadFileTree()
        }

        btn_backup?.setOnClickListener {
            startSelectDir(REQUEST_BACKUP_DIR)
        }

        btn_restore?.setOnClickListener {
            startSelectDir(REQUEST_RESTORE_DIR)
        }
    }

    fun startSelectDir(requestCode: Int) {
        val backupIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(backupIntent, requestCode)
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)
        if (reqCode == REQUEST_PUBLIC_FILE && resCode == Activity.RESULT_OK) {
            var uri: Uri? = null
            if (data != null) {
                uri = data.data
                val destFilePath = FileUtil.privateDbDirPath + SCPConstants.DETAIL_DB_NAME
                val destFile = File(destFilePath)
                uri?.let {
                    doAsync {
                        if (copyFileFromUri(this@DownloadActivity, uri, destFile)) {
                            DetailDatabase.getInstance()
                            FileUtil.checkDetailDb()
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
        } else if (reqCode == REQUEST_BACKUP_DIR && resCode == Activity.RESULT_OK) {
            data?.data?.let { uriTree ->
                // 创建所选目录的DocumentFile，可以使用它进行文件操作
                val root = DocumentFile.fromTreeUri(this, uriTree)
                root?.let {
                    doAsync {
                        val dbFile = if (root.findFile(INFO_DB_NAME) != null) {
                            root.findFile(INFO_DB_NAME)
                        } else {
                            root.createFile("*/*", INFO_DB_NAME)
                        }
                        val prefFile = if (root.findFile(PREF_NAME) != null) {
                            root.findFile(PREF_NAME)
                        } else {
                            root.createFile("application/xml", PREF_NAME)
                        }
                        val rawDbFile = File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}")
                        val rawPrefFile = File("${FileUtil.privatePrefDirPath}${PREF_NAME}")
                        dbFile?.let {
                            copyFileToUri(this@DownloadActivity, rawDbFile, dbFile.uri)
                        }
                        prefFile?.let {
                            copyFileToUri(this@DownloadActivity, rawPrefFile, prefFile.uri)
                        }
                        uiThread {
                            toast("备份完成")
                        }
                    }
                }
            }
        } else if (reqCode == REQUEST_RESTORE_DIR && resCode == Activity.RESULT_OK) {
            data?.data?.let { uriTree ->
                // 创建所选目录的DocumentFile，可以使用它进行文件操作
                val root = DocumentFile.fromTreeUri(this, uriTree)
                root?.let {
                    doAsync {
                        val rawDbFile = File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}")
                        val rawPrefFile = File("${FileUtil.privatePrefDirPath}${PREF_NAME}")
                        if (root.findFile(INFO_DB_NAME) != null) {
                            val dbFile = root.findFile(INFO_DB_NAME)
                            dbFile?.let {
                                copyFileFromUri(this@DownloadActivity, dbFile.uri, rawDbFile)
                            }
                        }
                        if (root.findFile(PREF_NAME) != null) {
                            val prefFile = root.findFile(PREF_NAME)
                            prefFile?.let {
                                copyFileFromUri(this@DownloadActivity, prefFile.uri, rawPrefFile)
                            }
                        }
                        AppInfoDatabase.getNewInstance()
                        uiThread {
                            toast("恢复完成")
                        }
                    }
                }
            }
        }
    }
}
