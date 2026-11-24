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
import androidx.lifecycle.lifecycleScope
import info.free.scp.R
import info.free.scp.components.CommonTopBar
import info.free.scp.data.backup.AndroidAppDataBackupRepository
import info.free.scp.data.backup.AppDataBackupRepository
import info.free.scp.ui.MainTheme
import info.free.scp.ui.user.DownloadScreen
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadActivity : BaseActivity() {
    private val backupRepository: AppDataBackupRepository = AndroidAppDataBackupRepository()
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

    private fun importDetailDatabase(uri: Uri) =
        runFileOperation("正文库已通过校验，下次冷启动时生效", "正文库导入失败") {
            backupRepository.importDetailDatabase(uri)
        }

    private fun backupAppData(uriTree: Uri) = runFileOperation("备份完成", "备份失败") {
        backupRepository.backupAppData(uriTree)
    }

    private fun restoreAppData(uriTree: Uri) =
        runFileOperation("备份已通过校验，下次冷启动时恢复", "恢复准备失败") {
            backupRepository.restoreAppData(uriTree)
        }

    private fun runFileOperation(success: String, failure: String, operation: () -> Result<Unit>) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { operation() }
            val error = result.exceptionOrNull()
            if (error == null) showToast(success)
            else AlertDialog.Builder(this@DownloadActivity)
                .setTitle(failure)
                .setMessage(error.message ?: "文件读写失败")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
