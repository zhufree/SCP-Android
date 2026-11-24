package info.free.scp.data.backup

import android.content.Context
import android.database.Cursor
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import info.free.scp.data.update.deleteFile
import info.free.scp.data.update.useRoom
import info.free.scp.db.AppInfoDatabase
import java.io.File
import java.io.RandomAccessFile

internal const val VACUUM_SNAPSHOT_SQL = "VACUUM INTO ?"

internal val USER_TABLES = listOf("records", "like_table", "like_box_table", "draft")

internal fun snapshotUserDatabase(context: Context, target: File) {
    val room = AppInfoDatabase.getInstance()
    val source = room.openHelper.writableDatabase
    val sqliteVersion = source.query("SELECT sqlite_version()").use {
        check(it.moveToFirst())
        it.getString(0).split('.').map(String::toInt)
    }
    if (sqliteVersion[0] > 3 || (sqliteVersion[0] == 3 && sqliteVersion[1] >= 27)) {
        deleteFile(target)
        source.execSQL(VACUUM_SNAPSHOT_SQL, arrayOf(target.path))
    } else {
        // Older Android SQLite has no VACUUM INTO. Copy the fixed Room schema from one
        // transaction so all tables include committed WAL writes from the same snapshot.
        AppInfoDatabase.builder(context, target.absolutePath)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build().useRoom { destinationRoom ->
                val destination = destinationRoom.openHelper.writableDatabase
                room.runInTransaction {
                    destinationRoom.runInTransaction {
                        (USER_TABLES + "sqlite_sequence").forEach { table -> copyTable(source, destination, table) }
                    }
                }
            }
    }
    RandomAccessFile(target, "rw").use { it.fd.sync() }
}

private fun copyTable(source: SupportSQLiteDatabase, target: SupportSQLiteDatabase, table: String) {
    target.execSQL("DELETE FROM `$table`")
    source.query("SELECT * FROM `$table`").use { rows ->
        val columns = rows.columnNames.joinToString(",") { "`$it`" }
        val parameters = List(rows.columnCount) { "?" }.joinToString(",")
        val sql = "INSERT INTO `$table` ($columns) VALUES ($parameters)"
        while (rows.moveToNext()) {
            val values = Array<Any?>(rows.columnCount) { column ->
                when (rows.getType(column)) {
                    Cursor.FIELD_TYPE_NULL -> null
                    Cursor.FIELD_TYPE_INTEGER -> rows.getLong(column)
                    Cursor.FIELD_TYPE_FLOAT -> rows.getDouble(column)
                    Cursor.FIELD_TYPE_BLOB -> rows.getBlob(column)
                    else -> rows.getString(column)
                }
            }
            target.execSQL(sql, values)
        }
    }
}
