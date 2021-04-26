package info.free.scp.view.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.APP_PREF_NAME
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.LEVEL_PREF_NAME
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
import java.util.*


class DownloadActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        baseToolbar = download_toolbar

        tv_go_download.setOnClickListener {
            if (Date().before(Date(SCPConstants.FREE_TIME))) { // 4.28之前，跳转百度网盘
                val panCode = "链接: https://pan.baidu.com/s/1c3Y2ZNhVzvUycB8uaXTV_g 提取码: icu5 复制这段内容后打开百度网盘手机App，操作更方便哦"
                Utils.copy(this, panCode)
                toast("已复制百度网盘链接")
            } else {
                val intent = Utils.getUrlIntent("https://mianbaoduo.com/o/bread/YZiZl51u")
                startActivity(intent)
            }
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
            // 备份
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
                        val levelPrefFile = if (root.findFile(LEVEL_PREF_NAME) != null) {
                            root.findFile(LEVEL_PREF_NAME)
                        } else {
                            root.createFile("application/xml", LEVEL_PREF_NAME)
                        }
                        val appPrefFile = if (root.findFile(APP_PREF_NAME) != null) {
                            root.findFile(APP_PREF_NAME)
                        } else {
                            root.createFile("application/xml", APP_PREF_NAME)
                        }
                        val rawDbFile = File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}")
                        val rawLevelPrefFile = File("${FileUtil.privatePrefDirPath}${LEVEL_PREF_NAME}")
                        val rawAppPrefFile = File("${FileUtil.privatePrefDirPath}${APP_PREF_NAME}")
                        dbFile?.let {
                            copyFileToUri(this@DownloadActivity, rawDbFile, dbFile.uri)
                        }
                        levelPrefFile?.let {
                            copyFileToUri(this@DownloadActivity, rawLevelPrefFile, levelPrefFile.uri)
                        }
                        appPrefFile?.let {
                            copyFileToUri(this@DownloadActivity, rawAppPrefFile, appPrefFile.uri)
                        }
                        uiThread {
                            toast("备份完成")
                        }
                    }
                }
            }
        } else if (reqCode == REQUEST_RESTORE_DIR && resCode == Activity.RESULT_OK) {
            // 恢复
            data?.data?.let { uriTree ->
                val root = DocumentFile.fromTreeUri(this, uriTree)
                root?.let {
                    doAsync {
                        AppInfoDatabase.getInstance().close()
                        val rawDbFile = File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}")
                        val rawLevelPrefFile = File("${FileUtil.privatePrefDirPath}${LEVEL_PREF_NAME}")
                        val rawAppPrefFile = File("${FileUtil.privatePrefDirPath}${APP_PREF_NAME}")
                        if (root.findFile(INFO_DB_NAME) != null) {
                            AppInfoDatabase.getInstance()
                            rawDbFile.delete()
                            File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}-shm").delete()
                            File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}-wal").delete()
                            val dbFile = root.findFile(INFO_DB_NAME)
                            dbFile?.let {
                                copyFileFromUri(this@DownloadActivity, dbFile.uri, rawDbFile)
                            }
                        }
                        if (root.findFile(LEVEL_PREF_NAME) != null) {
                            val prefFile = root.findFile(LEVEL_PREF_NAME)
                            prefFile?.let {
                                copyFileFromUri(this@DownloadActivity, prefFile.uri, rawLevelPrefFile)
                            }
                        }
                        if (root.findFile(APP_PREF_NAME) != null) {
                            val prefFile = root.findFile(APP_PREF_NAME)
                            prefFile?.let {
                                copyFileFromUri(this@DownloadActivity, prefFile.uri, rawAppPrefFile)
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
