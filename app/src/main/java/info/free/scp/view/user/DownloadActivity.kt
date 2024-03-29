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
import info.free.scp.databinding.ActivityDownloadBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
import info.free.scp.util.*
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.*
import java.io.File


class DownloadActivity : BaseActivity() {

    private lateinit var binding: ActivityDownloadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseToolbar = binding.downloadToolbar

        val downloadTitles = listOf("百度网盘", "飞书", "夸克", "面包多", "Google Drive")
        val downloadLinks = listOf(
            "https://pan.baidu.com/s/17MwfLiMmMxqq2WX_tzmGSw?pwd=t9ch",
            "https://gj5i5wsqre.feishu.cn/drive/folder/fldcnpvwVcYN4iCrUQBCPTgF25e",
            "https://pan.quark.cn/s/bc11451cf980",
            "https://mbd.pub/o/bread/mbd-YZiZl51u",
            "https://drive.google.com/drive/folders/1iGSW2u_-RuAS8HxebBzV-oCOgAZnVUr5?usp=sharing"
        )
        binding.btnGoDownload.setOnClickListener {
            selector("选择下载线路", downloadTitles) { _, i ->
                Utils.openUrl(downloadLinks[i])
            }
        }
        binding.btnCheckSpider.setOnClickListener {
            Utils.openUrl("https://gj5i5wsqre.feishu.cn/docx/X8BvdDE5ioFjJDx8aQKcTMIHnMd")
        }

        arrayOf(
            binding.btnGoDownload,
            binding.btnSelectFile,
            binding.btnCheckSpider,
            binding.btnBackup,
            binding.btnRestore
        ).forEach {
            it.background = ThemeUtil.customShape(
                ThemeUtil.linkBlue,
                0, 0, dip(24)
            )
        }

        binding.btnSelectFile.setOnClickListener {
            requestReadFileTree()
        }

        binding.btnBackup.setOnClickListener {
            startSelectDir(REQUEST_BACKUP_DIR)
        }

        binding.btnRestore.setOnClickListener {
            startSelectDir(REQUEST_RESTORE_DIR)
        }
    }

    private fun startSelectDir(requestCode: Int) {
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
                        val dbFile = root.createFile("*/*", INFO_DB_NAME)
                        val levelPrefFile = root.createFile("application/xml", LEVEL_PREF_NAME)
                        val appPrefFile = root.createFile("application/xml", APP_PREF_NAME)
                        val rawDbFile = File("${FileUtil.privateDbDirPath}${INFO_DB_NAME}")
                        val rawLevelPrefFile =
                            File("${FileUtil.privatePrefDirPath}${LEVEL_PREF_NAME}")
                        val rawAppPrefFile = File("${FileUtil.privatePrefDirPath}${APP_PREF_NAME}")
                        dbFile?.let {
                            copyFileToUri(this@DownloadActivity, rawDbFile, dbFile.uri)
                        }
                        levelPrefFile?.let {
                            copyFileToUri(
                                this@DownloadActivity,
                                rawLevelPrefFile,
                                levelPrefFile.uri
                            )
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
                        val rawLevelPrefFile =
                            File("${FileUtil.privatePrefDirPath}${LEVEL_PREF_NAME}")
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
                                rawLevelPrefFile.delete()
                                copyFileFromUri(
                                    this@DownloadActivity,
                                    prefFile.uri,
                                    rawLevelPrefFile
                                )
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
                            toast("恢复完成，部分数据可能需要重启APP后生效")
                        }
                    }
                }
            }
        }
    }
}
