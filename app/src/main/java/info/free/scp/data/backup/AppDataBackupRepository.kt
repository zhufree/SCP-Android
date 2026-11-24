package info.free.scp.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import info.free.scp.ScpApplication
import info.free.scp.data.update.DETAIL_IMPORT_LOCK
import info.free.scp.data.update.UpdateDatabases
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.FileUtil
import info.free.scp.util.copyFileFromUri
import info.free.scp.util.copyFileToUri
import java.io.File

interface AppDataBackupRepository {
    fun importDetailDatabase(uri: Uri): Boolean
    fun backupAppData(uriTree: Uri): Boolean
    fun restoreAppData(uriTree: Uri): Boolean
}

class AndroidAppDataBackupRepository(
    private val context: Context = ScpApplication.context,
    private val backupFilePlan: BackupFilePlan = BackupFilePlan(
        FileUtil.privateDbDirPath,
        FileUtil.privatePrefDirPath
    )
) : AppDataBackupRepository {
    override fun importDetailDatabase(uri: Uri): Boolean = synchronized(DETAIL_IMPORT_LOCK) {
        runCatching {
            checkNotNull(context.contentResolver.openInputStream(uri)).use {
                UpdateDatabases(context).stageLegacyDetail(it)
            }
        }.isSuccess
    }

    override fun backupAppData(uriTree: Uri): Boolean {
        val root = DocumentFile.fromTreeUri(context, uriTree) ?: return false
        backupFilePlan.files.forEach { spec ->
            val destination = root.findFile(spec.fileName)
                ?: root.createFile(spec.mimeType, spec.fileName)
            destination?.let {
                copyFileToUri(context, File(spec.localPath), it.uri)
            }
        }
        return true
    }

    override fun restoreAppData(uriTree: Uri): Boolean {
        val root = DocumentFile.fromTreeUri(context, uriTree) ?: return false
        AppInfoDatabase.getInstance().close()
        backupFilePlan.files.forEach { spec ->
            root.findFile(spec.fileName)?.let { source ->
                File(spec.localPath).delete()
                spec.sidecarPaths.forEach { File(it).delete() }
                copyFileFromUri(context, source.uri, File(spec.localPath))
            }
        }
        AppInfoDatabase.getNewInstance()
        // Room opens lazily, so exercise the restored file and its upgrade now.
        return runCatching {
            AppInfoDatabase.getInstance().openHelper.writableDatabase
        }.isSuccess
    }
}
