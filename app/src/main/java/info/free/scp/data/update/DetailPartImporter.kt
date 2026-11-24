package info.free.scp.data.update

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import info.free.scp.util.FileUtil
import java.io.File

internal val DETAIL_IMPORT_LOCK = Any()
private const val ROW_BYTES_LIMIT = 2 * MIB - 4096
private const val BATCH_ROWS = 32

internal data class DetailInventory(val fullImport: Boolean, val parts: Map<String, InstalledPart>)

internal class DetailPartImporter(private val databases: UpdateDatabases) {

    fun inventory(): DetailInventory =
        if (!databases.detailFile.isFile) DetailInventory(false, emptyMap())
        else openSqlite(databases.detailFile).use { db ->
            if (!db.hasTable("x_part_state")) DetailInventory(true, emptyMap())
            else DetailInventory(false, db.rawQuery("SELECT part_id, parts_version, digest FROM x_part_state", null).use {
                buildMap {
                    while (it.moveToNext()) put(it.getString(0), InstalledPart(it.getString(1), it.getString(2)))
                }
            })
        }

    fun validate(file: File, manifest: UpdateManifest, part: DetailPart) = openSqlite(file).use { db ->
        require(db.scalar("PRAGMA user_version") == "0") { "正文块 user_version 必须为 0" }
        require(db.scalar("PRAGMA quick_check") == "ok") { "正文块完整性检查失败" }
        db.requireColumns("scp_detail", DETAIL_COLUMNS)
        val meta = db.spiderMeta()
        requireSupportedContract(meta["contract_version"].orEmpty())
        require(meta["export_id"] == part.exportId && meta["part_id"] == part.id &&
            meta["parts_version"] == manifest.partsVersion && meta["content_digest"] == part.digest &&
            meta["rows"]?.toLongOrNull() == part.rows && db.scalar("SELECT count(*) FROM scp_detail").toLong() == part.rows
        ) { "正文块 spider_meta 或行数与清单不一致" }
    }

    fun install(file: File, manifest: UpdateManifest, part: DetailPart) = synchronized(DETAIL_IMPORT_LOCK) {
        check(!databases.legacyDetailPending) { "请先重启应用以完成整库导入" }
        // Read again under the same lock as the legacy import path.
        val installed = inventory().parts
        require(installed.values.none { it.version.toInt() > manifest.partsVersion.toInt() }) {
            "清单分块规则早于设备已安装的版本"
        }
        val plan = planParts(manifest.partsVersion, manifest.parts, installed)
        if (plan[part.id] == PartAction.SKIP) return@synchronized
        val retired = plan.filterValues { it == PartAction.DELETE }.keys
        databases.withDetailTarget { target -> merge(file, target, manifest, part, retired) }
        FileUtil.checkDetailDb()
    }

    private fun merge(file: File, target: File, manifest: UpdateManifest, part: DetailPart, retired: Set<String>) {
        var committed = false
        try {
            openSqlite(file, writable = true).use { source ->
                source.execSQL("ATTACH DATABASE ? AS local", arrayOf(target.path))
                try {
                    val sql = PartMergeSql("local")
                    source.immediate {
                        prepare(source, sql, retired)
                        source.execSQL(sql.upsertFromPart)
                        removeRows(source, sql, part.id, "main.scp_detail")
                        source.execSQL(sql.recordLinks("main.scp_detail"), arrayOf(part.id))
                        writeState(source, sql, manifest, part)
                    }
                    committed = true
                } finally {
                    runCatching { source.execSQL("DETACH DATABASE local") }.onFailure {
                        Log.w(UPDATE_LOG, "Detail part detach failed", it)
                    }
                }
            }
        } catch (failure: Exception) {
            Log.w(UPDATE_LOG, "Detail part ATTACH failed", failure)
            if (!committed) {
                try {
                    mergeByCursor(file, target, manifest, part, retired)
                } catch (fallbackFailure: Exception) {
                    fallbackFailure.addSuppressed(failure)
                    throw fallbackFailure
                }
            }
        }
    }

    private fun mergeByCursor(file: File, targetFile: File, manifest: UpdateManifest, part: DetailPart, retired: Set<String>) {
        openSqlite(file).use { source ->
            // Leave room for the cursor window's row and field headers.
            require(source.scalar("""
                SELECT EXISTS(SELECT 1 FROM scp_detail WHERE
                    length(CAST(link AS BLOB)) + coalesce(length(CAST(detail AS BLOB)), 0) +
                    coalesce(length(CAST(tags AS BLOB)), 0) > $ROW_BYTES_LIMIT)
            """.trimIndent()) == "0") { "正文块中有单行超过游标复制上限（2 MB）" }

            openSqlite(targetFile, writable = true, wal = true).use { target ->
                val sql = PartMergeSql("main")
                target.immediate {
                    prepare(target, sql, retired)
                    target.execSQL("CREATE TEMP TABLE part_snapshot (link TEXT NOT NULL PRIMARY KEY)")
                    var lastLink: String? = null
                    while (true) {
                        val previous = lastLink
                        val where = if (previous == null) "" else "WHERE link > ?"
                        val query = "SELECT link, detail, not_found, tags FROM scp_detail $where ORDER BY link LIMIT $BATCH_ROWS"
                        source.rawQuery(query, previous?.let { arrayOf(it) }).use { cursor ->
                            while (cursor.moveToNext()) {
                                val link = cursor.getString(0)
                                target.execSQL(sql.upsertRow, arrayOf<Any?>(
                                    link, cursor.getString(1), if (cursor.isNull(2)) null else cursor.getLong(2), cursor.getString(3)
                                ))
                                target.execSQL("INSERT INTO temp.part_snapshot (link) VALUES (?)", arrayOf(link))
                                lastLink = link
                            }
                        }
                        if (lastLink == previous) break
                    }
                    removeRows(target, sql, part.id, "temp.part_snapshot")
                    target.execSQL(sql.recordLinks("temp.part_snapshot"), arrayOf(part.id))
                    target.execSQL("DROP TABLE temp.part_snapshot")
                    writeState(target, sql, manifest, part)
                }
            }
        }
    }

    private fun prepare(db: SQLiteDatabase, sql: PartMergeSql, retired: Set<String>) {
        db.execSQL(sql.createOwnership)
        db.execSQL(sql.createState)
        retired.forEach { id ->
            removeRows(db, sql, id)
            db.execSQL(sql.deleteState, arrayOf(id))
        }
    }

    private fun removeRows(db: SQLiteDatabase, sql: PartMergeSql, id: String, keepLinks: String? = null) {
        sql.removeRows(keepLinks).forEach { db.execSQL(it, arrayOf(id)) }
    }

    private fun writeState(db: SQLiteDatabase, sql: PartMergeSql, manifest: UpdateManifest, part: DetailPart) {
        db.execSQL(sql.insertState, arrayOf<Any>(
            part.id, manifest.partsVersion, part.digest, part.exportId, part.file.sha256, part.rows, System.currentTimeMillis()
        ))
    }

    private fun SQLiteDatabase.immediate(block: () -> Unit) {
        // Android's non-exclusive transaction maps to BEGIN IMMEDIATE on its pinned connection.
        beginTransactionNonExclusive()
        try {
            block()
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
    }
}
