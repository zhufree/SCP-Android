package info.free.scp.view.user

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.APP_PREF_NAME
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.LEVEL_PREF_NAME
import info.free.scp.databinding.ActivityDownloadBinding
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
import info.free.scp.util.FileUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.util.copyFileFromUri
import info.free.scp.util.copyFileToUri
import info.free.scp.view.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadActivity : BaseActivity() {

    private lateinit var binding: ActivityDownloadBinding
    private val selectDatabaseLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(::importDetailDatabase)
        }
    private val backupDirectoryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let(::backupAppData)
        }
    private val restoreDirectoryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let(::restoreAppData)
        }

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
            AlertDialog.Builder(this)
                .setTitle("选择下载线路")
                .setItems(downloadTitles.toTypedArray()) { _, index ->
                    Utils.openUrl(downloadLinks[index])
                }
                .show()
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
                0,
                0,
                Utils.dp2px(24, this)
            )
        }

        binding.btnSelectFile.setOnClickListener {
            selectDatabaseLauncher.launch(arrayOf("*/*"))
        }

        binding.btnBackup.setOnClickListener {
            backupDirectoryLauncher.launch(null)
        }

        binding.btnRestore.setOnClickListener {
            restoreDirectoryLauncher.launch(null)
        }
    }

    private fun importDetailDatabase(uri: Uri) {
        val destFile = File(FileUtil.privateDbDirPath + SCPConstants.DETAIL_DB_NAME)
        lifecycleScope.launch(Dispatchers.IO) {
            val success = runCatching {
                copyFileFromUri(this@DownloadActivity, uri, destFile)
            }.getOrDefault(false)
            if (success) {
                DetailDatabase.getInstance()
                FileUtil.checkDetailDb()
            }
            withContext(Dispatchers.Main) {
                showToast(if (success) "数据库已加载" else "数据库读取失败")
            }
        }
    }

    private fun backupAppData(uriTree: Uri) {
        val root = DocumentFile.fromTreeUri(this, uriTree) ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val dbFile = ensureDocumentFile(root, INFO_DB_NAME, "*/*")
            val levelPrefFile = ensureDocumentFile(root, LEVEL_PREF_NAME, "application/xml")
            val appPrefFile = ensureDocumentFile(root, APP_PREF_NAME, "application/xml")
            val rawDbFile = File("${FileUtil.privateDbDirPath}$INFO_DB_NAME")
            val rawLevelPrefFile = File("${FileUtil.privatePrefDirPath}$LEVEL_PREF_NAME")
            val rawAppPrefFile = File("${FileUtil.privatePrefDirPath}$APP_PREF_NAME")
            dbFile?.let { copyFileToUri(this@DownloadActivity, rawDbFile, it.uri) }
            levelPrefFile?.let { copyFileToUri(this@DownloadActivity, rawLevelPrefFile, it.uri) }
            appPrefFile?.let { copyFileToUri(this@DownloadActivity, rawAppPrefFile, it.uri) }
            withContext(Dispatchers.Main) {
                showToast("备份完成")
            }
        }
    }

    private fun restoreAppData(uriTree: Uri) {
        val root = DocumentFile.fromTreeUri(this, uriTree) ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            AppInfoDatabase.getInstance().close()
            val rawDbFile = File("${FileUtil.privateDbDirPath}$INFO_DB_NAME")
            val rawLevelPrefFile = File("${FileUtil.privatePrefDirPath}$LEVEL_PREF_NAME")
            val rawAppPrefFile = File("${FileUtil.privatePrefDirPath}$APP_PREF_NAME")

            root.findFile(INFO_DB_NAME)?.let { dbFile ->
                rawDbFile.delete()
                File("${FileUtil.privateDbDirPath}$INFO_DB_NAME-shm").delete()
                File("${FileUtil.privateDbDirPath}$INFO_DB_NAME-wal").delete()
                copyFileFromUri(this@DownloadActivity, dbFile.uri, rawDbFile)
            }
            root.findFile(LEVEL_PREF_NAME)?.let { prefFile ->
                rawLevelPrefFile.delete()
                copyFileFromUri(this@DownloadActivity, prefFile.uri, rawLevelPrefFile)
            }
            root.findFile(APP_PREF_NAME)?.let { prefFile ->
                rawAppPrefFile.delete()
                copyFileFromUri(this@DownloadActivity, prefFile.uri, rawAppPrefFile)
            }
            AppInfoDatabase.getNewInstance()
            withContext(Dispatchers.Main) {
                showToast("恢复完成，部分数据可能需要重启应用后生效")
            }
        }
    }

    private fun ensureDocumentFile(
        root: DocumentFile,
        fileName: String,
        mimeType: String
    ): DocumentFile? {
        return root.findFile(fileName) ?: root.createFile(mimeType, fileName)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
