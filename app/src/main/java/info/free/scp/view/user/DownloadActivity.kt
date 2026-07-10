package info.free.scp.view.user

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.APP_PREF_NAME
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.LEVEL_PREF_NAME
import info.free.scp.components.CommonTopBar
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
import info.free.scp.ui.MainTheme
import info.free.scp.ui.user.DownloadScreen
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
        setContent {
            MainTheme {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = getString(R.string.download_settings),
                            onBackPressed = { finish() }
                        )
                    },
                    backgroundColor = Color(ThemeUtil.containerBg)
                ) { padding ->
                    DownloadScreen(
                        onOpenDownload = { showDownloadRoutes() },
                        onSelectDatabase = { selectDatabaseLauncher.launch(arrayOf("*/*")) },
                        onOpenSpiderDoc = {
                            Utils.openUrl("https://gj5i5wsqre.feishu.cn/docx/X8BvdDE5ioFjJDx8aQKcTMIHnMd")
                        },
                        onBackup = { backupDirectoryLauncher.launch(null) },
                        onRestore = { restoreDirectoryLauncher.launch(null) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }

    private fun showDownloadRoutes() {
        val downloadTitles = listOf(
            "\u767e\u5ea6\u7f51\u76d8",
            "\u98de\u4e66",
            "\u5938\u514b",
            "\u9762\u5305\u591a",
            "Google Drive"
        )
        val downloadLinks = listOf(
            "https://pan.baidu.com/s/17MwfLiMmMxqq2WX_tzmGSw?pwd=t9ch",
            "https://gj5i5wsqre.feishu.cn/drive/folder/fldcnpvwVcYN4iCrUQBCPTgF25e",
            "https://pan.quark.cn/s/bc11451cf980",
            "https://mbd.pub/o/bread/mbd-YZiZl51u",
            "https://drive.google.com/drive/folders/1iGSW2u_-RuAS8HxebBzV-oCOgAZnVUr5?usp=sharing"
        )
        AlertDialog.Builder(this)
            .setTitle("\u9009\u62e9\u4e0b\u8f7d\u7ebf\u8def")
            .setItems(downloadTitles.toTypedArray()) { _, index ->
                Utils.openUrl(downloadLinks[index])
            }
            .show()
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
                showToast(if (success) "\u6570\u636e\u5e93\u5df2\u52a0\u8f7d" else "\u6570\u636e\u5e93\u8bfb\u53d6\u5931\u8d25")
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
                showToast("\u5907\u4efd\u5b8c\u6210")
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
                showToast("\u6062\u590d\u5b8c\u6210\uff0c\u90e8\u5206\u6570\u636e\u53ef\u80fd\u9700\u8981\u91cd\u542f\u5e94\u7528\u540e\u751f\u6548")
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
