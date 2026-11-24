package info.free.scp.data.backup

import android.content.Context
import android.net.Uri
import android.util.Xml
import androidx.documentfile.provider.DocumentFile
import androidx.room.RoomDatabase
import info.free.scp.ScpApplication
import info.free.scp.data.update.DETAIL_IMPORT_LOCK
import info.free.scp.data.update.UpdateDatabases
import info.free.scp.data.update.checkpointBeforeSwap
import info.free.scp.data.update.copyBounded
import info.free.scp.data.update.deleteFile
import info.free.scp.data.update.deleteSidecars
import info.free.scp.data.update.hasTable
import info.free.scp.data.update.moveFile
import info.free.scp.data.update.openSqlite
import info.free.scp.data.update.requireNonEmpty
import info.free.scp.data.update.scalar
import info.free.scp.data.update.validateDatabase
import info.free.scp.data.update.useRoom
import info.free.scp.db.APP_INFO_VERSION
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpTable.LIKE_AND_READ_TABLE_NAME
import info.free.scp.db.ScpTable.VIEW_LIST_TABLE_NAME
import info.free.scp.util.copyFileToUri
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.RandomAccessFile

interface AppDataBackupRepository {
    fun importDetailDatabase(uri: Uri): Result<Unit>
    fun backupAppData(uriTree: Uri): Result<Unit>
    fun restoreAppData(uriTree: Uri): Result<Unit>
}

private val backupLock = Any()

class AndroidAppDataBackupRepository(
    private val context: Context = ScpApplication.context
) : AppDataBackupRepository {
    private val databaseDirectory = context.getDatabasePath("path").parentFile!!
    private val preferencesDirectory = File(context.applicationInfo.dataDir, "shared_prefs")
    private val backupFilePlan = BackupFilePlan(databaseDirectory.path, preferencesDirectory.path)
    private val pending = File(context.filesDir, "user-data.pending")
    private val working = File(context.filesDir, "user-data.working")
    private val targets = backupFilePlan.files.map { File(it.localPath) }

    override fun importDetailDatabase(uri: Uri): Result<Unit> = synchronized(DETAIL_IMPORT_LOCK) {
        runCatching {
            requireNotNull(context.contentResolver.openInputStream(uri)) { "无法打开正文库文件" }.use {
                UpdateDatabases(context).stageLegacyDetail(it)
            }
        }
    }

    override fun backupAppData(uriTree: Uri): Result<Unit> = synchronized(backupLock) {
        runCatching {
            val root = requireNotNull(DocumentFile.fromTreeUri(context, uriTree)) { "无法打开备份目录" }
            withWorkingDirectory {
                snapshotUserDatabase(context, File(working, targets.first().name))
                targets.drop(1).forEach { pref ->
                    val snapshot = File(working, pref.name)
                    val previous = File(pref.path + ".bak")
                    val source = if (previous.isFile) previous else pref
                    if (source.exists()) source.inputStream().use { copyBounded(it, snapshot, Long.MAX_VALUE) }
                    else snapshot.writeText("<map />") // Android leaves unused preferences unwritten.
                    validatePreference(snapshot)
                }
                backupFilePlan.files.forEach { spec ->
                    val destination = root.findFile(spec.fileName) ?: root.createFile(spec.mimeType, spec.fileName)
                    requireNotNull(destination) { "无法创建 ${spec.fileName}，备份不完整" }
                    require(destination.isFile && destination.name == spec.fileName) {
                        "无法写入 ${spec.fileName}，备份不完整"
                    }
                    try {
                        copyFileToUri(context, File(working, spec.fileName), destination.uri)
                    } catch (failure: Exception) {
                        throw IllegalStateException("${spec.fileName} 写入失败，备份不完整", failure)
                    }
                }
            }
        }
    }

    override fun restoreAppData(uriTree: Uri): Result<Unit> = synchronized(backupLock) {
        runCatching {
            check(!pending.exists()) { "已有待恢复的备份，请先重启应用" }
            val root = requireNotNull(DocumentFile.fromTreeUri(context, uriTree)) { "无法打开备份目录" }
            withWorkingDirectory {
                targets.forEach { target ->
                    val source = requireNotNull(root.findFile(target.name)) { "备份缺少 ${target.name}" }
                    requireNotNull(context.contentResolver.openInputStream(source.uri)) { "无法读取 ${target.name}" }
                        .use { copyBounded(it, File(working, target.name), Long.MAX_VALUE) }
                }
                val database = File(working, targets.first().name)
                requireNonEmpty(database)
                openSqlite(database).use { db ->
                    validateDatabase(database, APP_INFO_VERSION, ::requiredUserTables, db::scalar, db::hasTable)
                }
                targets.drop(1).forEach { validatePreference(File(working, it.name)) }
                probeUserDatabase(database)
                checkpointBeforeSwap(database)
                deleteSidecars(database)
                RandomAccessFile(database, "rw").use { it.fd.sync() }
                moveFile(working, pending)
            }
        }
    }

    internal fun restoreAtColdStart(report: (String) -> Unit) {
        working.deleteRecursively()
        val database = targets.first()
        RestoreSwap(pending, targets).install(
            prepare = {
                check(databaseDirectory.isDirectory || databaseDirectory.mkdirs()) { "无法创建数据库目录" }
                check(preferencesDirectory.isDirectory || preferencesDirectory.mkdirs()) { "无法创建偏好目录" }
                checkpointBeforeSwap(database)
                // SharedPreferences itself uses .bak for interrupted writes. Resolve those first.
                targets.drop(1).forEach { pref ->
                    val previous = File(pref.path + ".bak")
                    if (previous.isFile) moveFile(previous, pref)
                }
            },
            clearSidecars = { if (it == database) deleteSidecars(it) },
            probe = {
                probeUserDatabase(database)
                targets.drop(1).forEach(::validatePreference)
            },
            report = report
        )
    }

    private fun withWorkingDirectory(action: () -> Unit) {
        check(working.deleteRecursively() && working.mkdir()) { "无法创建备份暂存目录" }
        try {
            action()
        } finally {
            working.deleteRecursively()
        }
    }

    private fun probeUserDatabase(file: File) {
        requireNonEmpty(file)
        AppInfoDatabase.builder(context, file.absolutePath).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build().useRoom { room ->
                val database = room.openHelper.writableDatabase
                USER_TABLES.forEach { table ->
                    database.query("SELECT * FROM `$table` LIMIT 1").use { it.moveToFirst() }
                }
            }
    }
}

internal fun requiredUserTables(version: Int): List<String> = when (version) {
    in 0..4 -> listOf(VIEW_LIST_TABLE_NAME, LIKE_AND_READ_TABLE_NAME)
    5 -> USER_TABLES.take(2)
    6 -> USER_TABLES - "like_box_table"
    else -> USER_TABLES
}

private fun validatePreference(file: File) {
    requireNonEmpty(file)
    file.inputStream().use {
        val parser = Xml.newPullParser()
        parser.setInput(it, null)
        var event = parser.nextToken()
        var map = false
        var valueType = ""
        while (event != XmlPullParser.END_DOCUMENT) {
            require(event != XmlPullParser.DOCDECL) { "${file.name} 不能包含 DTD" }
            if (event == XmlPullParser.START_TAG) {
                when (parser.depth) {
                    1 -> {
                        require(!map && parser.name == "map") { "${file.name} 不是偏好文件" }
                        map = true
                    }
                    2 -> {
                        valueType = parser.name
                        val key = parser.getAttributeValue(null, "name")
                        val value = parser.getAttributeValue(null, "value")
                        val valid = when (valueType) {
                            "boolean" -> value == "true" || value == "false"
                            "int" -> value?.toIntOrNull() != null
                            "long" -> value?.toLongOrNull() != null
                            "float" -> value?.toFloatOrNull() != null
                            "string", "set" -> true
                            else -> false
                        }
                        require(key != null && valid) { "${file.name} 包含无效偏好项" }
                    }
                    3 -> require(valueType == "set" && parser.name == "string") { "${file.name} 包含无效的偏好集合" }
                    else -> error("${file.name} 包含无效的偏好项")
                }
            }
            event = parser.nextToken()
        }
        require(map) { "${file.name} 缺少 map" }
    }
}
