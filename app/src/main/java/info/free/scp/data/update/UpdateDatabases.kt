package info.free.scp.data.update

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase
import java.io.File
import java.io.InputStream
import java.util.Locale

internal const val UPDATE_PREFS = "data_updates"
internal const val CATALOG_PENDING = "catalog_pending"
internal const val UPDATE_LOG = "ScpDataUpdates"

internal fun openSqlite(file: File, writable: Boolean = false, wal: Boolean = false): SQLiteDatabase {
    val access = if (writable) SQLiteDatabase.OPEN_READWRITE else SQLiteDatabase.OPEN_READONLY
    val journal = if (wal) SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING else 0
    return SQLiteDatabase.openDatabase(file.path, null, access or journal or SQLiteDatabase.NO_LOCALIZED_COLLATORS)
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
            require(total <= limit) { "数据文件超出大小限制" }
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

    fun prepareCatalog(file: File, expected: CatalogVersion) {
        openSqlite(file).use { db ->
            require(db.scalar("PRAGMA user_version") == "0") { "目录库 user_version 必须为 0" }
            require(db.scalar("PRAGMA quick_check") == "ok") { "目录库完整性检查失败" }
            db.requireColumns("scps", CATALOG_COLUMNS)
            require(db.scalar("SELECT EXISTS(SELECT 1 FROM scps WHERE sub_scp_type IS NULL)") == "0") {
                "目录库的 sub_scp_type 不能为 NULL"
            }
            val actual = if (db.hasTable("spider_meta")) catalogVersion(db.spiderMeta()) else CatalogVersion.Legacy
            require(actual.exportId == expected.exportId && actual.contractVersion == expected.contractVersion &&
                sameExportTime(actual, expected)) { "目录库 spider_meta 与清单不一致" }
        }

        // SQLiteOpenHelper writes a Room identity and user_version on first open. Check a
        // disposable copy so the staged archive remains exactly as the publisher supplied it.
        val checkFile = newDatabaseTemp()
        try {
            file.copyTo(checkFile, overwrite = true)
            val room = Room.databaseBuilder(context, ScpDatabase::class.java, checkFile.name)
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .allowMainThreadQueries()
                .build()
            try {
                room.openHelper.writableDatabase
            } finally {
                room.close()
            }
            moveFile(file, catalogSwap.staging)
            check(prefs.edit().putBoolean(CATALOG_PENDING, true).commit()) { "无法保存目录库待安装标记" }
        } finally {
            deleteSidecars(checkFile)
            deleteFile(checkFile)
        }
    }

    fun installAtColdStart() {
        // Interrupted downloads and Room probes never use the reserved staging names.
        listOfNotNull(detailFile.parentFile, context.cacheDir).forEach { directory ->
            directory.listFiles { file -> file.name.startsWith("scp-update-") }?.forEach { it.delete() }
        }
        // 22dbb68 disabled direct copying and switched initialization to createFromAsset.
        // Perform replacement before Room opens, including when an Activity is recreated.
        try {
            catalogSwap.recover(::probeCatalog)
            if (prefs.getBoolean(CATALOG_PENDING, false)) {
                if (catalogSwap.staging.isFile) {
                    val candidate = runCatching { pendingCatalog() }.getOrElse {
                        Log.w(UPDATE_LOG, "Discarding unreadable staged catalog", it)
                        null
                    }
                    val current = runCatching { installedCatalog() }.getOrNull()
                    if (candidate != null && newerCatalog(candidate, current)) installCatalog()
                    else {
                        deleteFile(catalogSwap.staging)
                        clearCatalogPending()
                    }
                } else {
                    clearCatalogPending()
                }
            } else {
                deleteFile(catalogSwap.staging)
            }
            val bundled = context.assets.open("scp_category_v2.meta.json").bufferedReader().use {
                readAssetVersion(it.readText())
            }
            if (newerCatalog(bundled, runCatching { installedCatalog() }.getOrNull())) {
                val file = newDatabaseTemp()
                try {
                    context.assets.open(SCP_DB_NAME).use { copyBounded(it, file, CATEGORY_LIMIT) }
                    prepareCatalog(file, bundled)
                    installCatalog()
                } finally {
                    deleteFile(file)
                }
            }
        } catch (failure: Exception) {
            Log.w(UPDATE_LOG, "Cold-start catalog installation failed", failure)
        }

        try {
            detailSwap.recover(::probeDetail)
            if (legacyDetailPending) {
                checkpointBeforeSwap(detailFile)
                detailSwap.install(::probeDetail)
            }
        } catch (failure: Exception) {
            Log.w(UPDATE_LOG, "Cold-start full detail installation failed", failure)
        }
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
            openSqlite(staged).use { db ->
                db.requireColumns("scp_detail", DETAIL_COLUMNS)
                require(db.scalar("PRAGMA user_version") in setOf("0", "2")) { "整库数据库版本不支持" }
                require(db.scalar("PRAGMA quick_check") == "ok") { "正文库完整性检查失败" }
                require(!db.hasTable("x_part_state") && !db.hasTable("x_detail_part")) { "请选择整库发布文件" }
            }
            moveFile(staged, detailSwap.staging)
        } finally {
            deleteFile(staged)
        }
    }

    private fun probeCatalog(file: File) = openSqlite(file).use { it.requireColumns("scps", CATALOG_COLUMNS) }

    private fun probeDetail(file: File) = openSqlite(file).use { it.requireColumns("scp_detail", DETAIL_COLUMNS) }

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
            val room = Room.databaseBuilder(context, DetailDatabase::class.java, file.name)
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .build()
            try {
                room.openHelper.writableDatabase
            } finally {
                room.close()
            }
            merge(file)
            checkpointBeforeSwap(file)
            deleteSidecars(file)
            moveFile(file, detailFile)
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

    private fun checkpointBeforeSwap(file: File) {
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
}
