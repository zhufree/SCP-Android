package info.free.scp.data.update

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil
import java.io.File
import java.io.InputStream
import java.util.Locale

internal const val UPDATE_PREFS = "data_updates"
internal const val CATALOG_PENDING = "catalog_pending"
internal const val UPDATE_LOG = "ScpDataUpdates"

internal fun openSqlite(file: File, writable: Boolean = false, wal: Boolean = false): SQLiteDatabase {
    val access = if (writable) SQLiteDatabase.OPEN_READWRITE else SQLiteDatabase.OPEN_READONLY
    val journal = if (wal) SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING else 0
    return SQLiteDatabase.openDatabase(file.path, null,
        access or journal or SQLiteDatabase.NO_LOCALIZED_COLLATORS, DatabaseErrorHandler { })
}

internal fun SQLiteDatabase.scalar(sql: String): String =
    rawQuery(sql, null).use {
        check(it.moveToFirst()) { "SQLite 查询无返回值" }
        it.getString(0)
    }

internal fun SQLiteDatabase.hasTable(name: String): Boolean =
    rawQuery("SELECT 1 FROM sqlite_master WHERE name = ? AND type = 'table'", arrayOf(name)).use { it.moveToFirst() }

internal fun SQLiteDatabase.requireColumns(table: String, expected: Map<String, RoomColumn>) {
    require(hasTable(table)) { "数据文件缺少 $table 表" }
    val actual = rawQuery("PRAGMA table_info($table)", null).use { cursor ->
        buildMap {
            while (cursor.moveToNext()) {
                put(cursor.getString(1), RoomColumn(cursor.getString(2).uppercase(Locale.ROOT), cursor.getInt(3) != 0, cursor.getInt(5)))
            }
        }
    }
    require(matchesRoomColumns(expected, actual)) { "$table 列结构与 Room 不一致" }
}

internal fun SQLiteDatabase.spiderMeta(): Map<String, String> =
    rawQuery("SELECT key, value FROM spider_meta", null).use { cursor ->
        buildMap {
            while (cursor.moveToNext()) {
                val key = cursor.getString(0)
                require(put(key, cursor.getString(1)) == null) { "spider_meta 键重复" }
            }
        }
    }

internal fun copyBounded(input: InputStream, target: File, limit: Long, onBytes: (Long) -> Unit = {}): Long {
    target.outputStream().use { output ->
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            total += count
            if (total > limit) throw ReleaseSourceFailure("数据文件超出大小限制")
            output.write(buffer, 0, count)
            onBytes(total)
        }
        output.fd.sync()
        return total
    }
}

internal class UpdateDatabases(private val context: Context) {
    private val prefs = context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
    private val catalogSwap = DatabaseSwap(context.getDatabasePath(SCP_DB_NAME), context.getDatabasePath("scp_category_v2.staging.db"))
    private val detailSwap = DatabaseSwap(context.getDatabasePath(DETAIL_DB_NAME), context.getDatabasePath("scp_detail_v2.staging.db"))

    val detailFile: File get() = detailSwap.live
    val legacyDetailPending: Boolean get() = detailSwap.staging.isFile

    fun installedCatalog(): CatalogVersion? = catalogVersionAt(catalogSwap.live)

    fun pendingCatalog(): CatalogVersion? =
        if (prefs.getBoolean(CATALOG_PENDING, false)) catalogVersionAt(catalogSwap.staging) else null

    private fun catalogVersionAt(file: File): CatalogVersion? =
        if (!file.isFile) null else openSqlite(file).use {
            if (it.hasTable("spider_meta")) {
                catalogVersion(it.spiderMeta(), requireSupported = false)
            } else {
                it.requireColumns("scps", CATALOG_COLUMNS)
                require(it.scalar("PRAGMA user_version") in setOf("0", "2")) { "旧目录库版本不支持" }
                CatalogVersion.Legacy
            }
        }

    fun newDatabaseTemp(): File {
        val directory = checkNotNull(detailFile.parentFile)
        check(directory.isDirectory || directory.mkdirs()) { "无法创建数据库目录" }
        return File.createTempFile("scp-update-", ".db", directory)
    }

    fun prepareCatalog(file: File, expected: CatalogVersion, beforeStaging: () -> Unit) {
        openSqlite(file).use { db ->
            require(db.scalar("PRAGMA user_version") == "0") { "目录库 user_version 必须为 0" }
            require(db.scalar("PRAGMA quick_check") == "ok") { "目录库完整性检查失败" }
            db.requireColumns("scps", CATALOG_COLUMNS)
            require(db.scalar("SELECT EXISTS(SELECT 1 FROM scps WHERE sub_scp_type IS NULL)") == "0") {
                "目录库的 sub_scp_type 不能为 NULL"
            }
            val actual = if (db.hasTable("spider_meta")) catalogVersion(db.spiderMeta()) else CatalogVersion.Legacy
            require(sameCatalogExport(actual, expected)) { "目录库 spider_meta 与清单不一致" }
        }

        // SQLiteOpenHelper writes a Room identity and user_version on first open. Check a
        // disposable copy so the staged archive remains exactly as the publisher supplied it.
        val checkFile = newDatabaseTemp()
        try {
            file.copyTo(checkFile, overwrite = true)
            probeCatalog(checkFile)
            beforeStaging()
            moveFile(file, catalogSwap.staging)
            check(prefs.edit().putBoolean(CATALOG_PENDING, true).commit()) { "无法保存目录库待安装标记" }
        } finally {
            deleteSidecars(checkFile)
            deleteFile(checkFile)
        }
    }

    fun installAtColdStart(report: (String) -> Unit) {
        // Interrupted downloads and Room probes never use the reserved staging names.
        listOfNotNull(detailFile.parentFile, context.cacheDir).forEach { directory ->
            directory.listFiles { file -> file.name.startsWith("scp-update-") }?.forEach { it.delete() }
        }
        try {
            if (catalogSwap.recover(::probeCatalog)) report("目录库更新已回滚")
            val pending = prefs.getBoolean(CATALOG_PENDING, false)
            if (!pending || !catalogSwap.staging.isFile) {
                discardStagedCatalog()
                if (pending) report("待安装的目录库文件缺失，请重新下载目录更新")
            } else if (!catalogSwap.live.exists() || runCatching { probeCatalog(catalogSwap.live) }.isSuccess) {
                val candidate = try {
                    probeCatalog(catalogSwap.staging)
                    pendingCatalog()
                } catch (failure: Exception) {
                    discardStagedCatalog()
                    throw failure
                }
                if (candidate != null && newerCatalog(candidate, installedCatalog())) installCatalog()
                else discardStagedCatalog()
            }
        } catch (failure: Exception) {
            Log.w(UPDATE_LOG, "Cold-start staged catalog installation failed", failure)
            report("目录库更新未完成，正在检查可用目录")
        }

        // Evaluate the live file independently even if the staged install or its checkpoint failed.
        try {
            if (recoverCatalog(catalogSwap.live, ::probeCatalog, ::installBundledCatalog)) {
                report("旧目录库无法读取，已恢复内置目录")
            }
            ScpDatabase.ready = true
        } catch (failure: Exception) {
            Log.w(UPDATE_LOG, "Cold-start catalog recovery failed", failure)
            report("目录库暂不可用，请检查可用空间后重启应用重试")
        }

        if (ScpDatabase.ready) {
            try {
                val bundled = bundledCatalogVersion()
                if (newerCatalog(bundled, installedCatalog())) installBundledCatalog()
            } catch (failure: Exception) {
                Log.w(UPDATE_LOG, "Bundled catalog update failed", failure)
                ScpDatabase.ready = runCatching { probeCatalog(catalogSwap.live) }.isSuccess
                report(if (ScpDatabase.ready) "内置目录更新未完成，将在下次冷启动重试"
                    else "目录库暂不可用，请检查可用空间后重启应用重试")
            }
        }

        try {
            // A swap backup only exists during a full import. The promoted file must still
            // meet full-import requirements; the restored local file may be an empty part store.
            if (detailSwap.recover(::probeFullDetail)) report("正文整库安装中断，已还原旧库")
            if (legacyDetailPending) {
                try {
                    validateFullDetail(detailSwap.staging)
                } catch (failure: Exception) {
                    deleteSidecars(detailSwap.staging)
                    deleteFile(detailSwap.staging)
                    throw failure
                }
                if (detailFile.exists() && runCatching { probeDetail(detailFile, requireContent = false) }.isFailure) {
                    quarantineDatabase(detailFile)
                }
                checkpointBeforeSwap(detailFile)
                detailSwap.install(::probeFullDetail)
                report("正文整库已安装")
            }
        } catch (failure: Exception) {
            Log.w(UPDATE_LOG, "Cold-start full detail installation failed", failure)
            report("正文整库安装未完成，请在离线与备份页面检查或重新导入")
        }

        if (detailFile.exists()) {
            DetailDatabase.ready = runCatching { probeDetail(detailFile, requireContent = false) }
                .onFailure {
                    Log.w(UPDATE_LOG, "Detail database unavailable", it)
                    report("本地正文库无法读取，已使用在线阅读，可重新导入正文库修复")
                }.isSuccess
        }
        val mode = if (DetailDatabase.ready) OFFLINE else ONLINE
        if (PreferenceUtil.getAppMode() != mode) PreferenceUtil.setAppMode(mode)
    }

    private fun bundledCatalogVersion(): CatalogVersion =
        context.assets.open("scp_category_v2.meta.json").bufferedReader().use {
            readAssetVersion(it.readText())
        }

    private fun installBundledCatalog() {
        val file = newDatabaseTemp()
        try {
            context.assets.open(SCP_DB_NAME).use { copyBounded(it, file, CATEGORY_LIMIT) }
            // Bundled recovery uses its own staging path, independent of a failed download.
            val bundled = bundledCatalogVersion()
            val actual = catalogVersionAt(file)
            require(actual != null && sameCatalogExport(actual, bundled)) { "内置目录元数据不一致" }
            probeCatalog(file)
            checkpointBeforeSwap(catalogSwap.live)
            DatabaseSwap(catalogSwap.live, file).install(::probeCatalog)
        } finally {
            deleteSidecars(file)
            deleteFile(file)
        }
    }

    private fun discardStagedCatalog() {
        deleteSidecars(catalogSwap.staging)
        deleteFile(catalogSwap.staging)
        if (prefs.contains(CATALOG_PENDING)) clearCatalogPending()
    }

    private fun installCatalog() {
        checkpointBeforeSwap(catalogSwap.live)
        catalogSwap.install(::probeCatalog)
        clearCatalogPending()
    }

    private fun clearCatalogPending() {
        check(prefs.edit().remove(CATALOG_PENDING).commit()) { "无法清除目录库待安装标记" }
    }

    fun stageLegacyDetail(input: InputStream) {
        val staged = newDatabaseTemp()
        try {
            copyBounded(input, staged, Long.MAX_VALUE)
            validateFullDetail(staged)
            checkpointBeforeSwap(staged)
            moveFile(staged, detailSwap.staging)
        } finally {
            deleteSidecars(staged)
            deleteFile(staged)
        }
    }

    private fun validateFullDetail(file: File) {
        requireNonEmpty(file)
        openSqlite(file).use { db ->
            validateDatabase(file, 2, { listOf("scp_detail") }, db::scalar, db::hasTable)
            require(db.scalar("PRAGMA user_version") in setOf("0", "2")) { "整库数据库版本不支持" }
            db.requireColumns("scp_detail", DETAIL_COLUMNS)
        }
        probeFullDetail(file)
    }

    private fun probeCatalog(file: File) {
        requireNonEmpty(file)
        openSqlite(file).use {
            require(it.hasTable("scps")) { "目录库缺少 scps 表" }
            require(it.scalar("PRAGMA user_version") in setOf("0", "2")) { "旧目录库版本不支持" }
        }
        ScpDatabase.builder(context, file.name).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build().useRoom { room ->
            room.openHelper.writableDatabase.query("SELECT * FROM scps LIMIT 1").use {
                require(it.moveToFirst() && it.getString(it.getColumnIndexOrThrow("link")).isNotBlank()) {
                    "目录库没有可读取的条目"
                }
            }
        }
    }

    private fun probeFullDetail(file: File) {
        requireNonEmpty(file)
        openSqlite(file, writable = true).use { db -> requireFullDetailSource(db::hasTable, db::scalar) }
        probeDetail(file, requireContent = true)
    }

    private fun probeDetail(file: File, requireContent: Boolean) {
        probeDetailDatabase(file, requireContent) { sql ->
            DetailDatabase.builder(context, file.name).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build().useRoom { room ->
                room.openHelper.writableDatabase.query(sql).use {
                    it.moveToFirst() && it.getString(1).isNotBlank()
                }
            }
        }
    }

    fun withDetailTarget(merge: (File) -> Unit) {
        if (detailFile.exists()) {
            // Run Room's existing open/migration path before the independent importer writes.
            checkNotNull(DetailDatabase.getInstance()).openHelper.writableDatabase
            merge(detailFile)
            runCatching { checkpointDetail(detailFile) }.onFailure {
                Log.w(UPDATE_LOG, "Detail WAL checkpoint deferred", it)
            }
            return
        }

        // A failed first import must not leave an empty database in use by Room.
        val file = newDatabaseTemp()
        try {
            DetailDatabase.builder(context, file.name).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build().useRoom {
                it.openHelper.writableDatabase
            }
            merge(file)
            checkpointBeforeSwap(file)
            deleteSidecars(file)
            moveFile(file, detailFile)
            DetailDatabase.ready = true
        } finally {
            deleteSidecars(file)
            deleteFile(file)
        }
    }

    private fun checkpointDetail(file: File) {
        openSqlite(file, writable = true, wal = true).use { db ->
            val truncated = runCatching { db.scalar("PRAGMA wal_checkpoint(TRUNCATE)") == "0" }.getOrDefault(false)
            if (!truncated) db.scalar("PRAGMA wal_checkpoint(PASSIVE)")
        }
    }
}

internal fun checkpointBeforeSwap(file: File) {
    if (!file.isFile) return
    val pendingWal = File(file.path + "-wal").length() > 0L
    val pendingJournal = File(file.path + "-journal").length() > 0L
    if (!pendingWal && !pendingJournal) return
    openSqlite(file, writable = true).use { db ->
        // Opening the schema recovers a hot rollback journal before the old file is backed up.
        if (pendingJournal) db.scalar("PRAGMA user_version")
        if (pendingWal) {
            check(db.scalar("PRAGMA wal_checkpoint(TRUNCATE)") == "0") { "数据库 WAL 尚未写回" }
        }
    }
}
