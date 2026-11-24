package info.free.scp.db

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.DraftModel
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.db.ScpTable.LIKE_AND_READ_TABLE_NAME
import info.free.scp.db.ScpTable.VIEW_LIST_TABLE_NAME
import info.free.scp.db.ScpTable.dropDetailTableSQL
import info.free.scp.db.ScpTable.dropScpTableSQL


@Database(entities = [ScpRecordModel::class, ScpLikeModel::class, DraftModel::class, ScpLikeBox::class], version = 8)
@TypeConverters(Converters::class)
abstract class AppInfoDatabase : RoomDatabase() {
    abstract fun likeAndReadDao(): LikeAndReadDao
    abstract fun readRecordDao(): ReadRecordDao
    abstract fun draftDao(): DraftDao

    companion object {
        private var INSTANCE: AppInfoDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 升级时删掉数据部分，只留info部分
                db.execSQL(dropDetailTableSQL)
                db.execSQL(dropScpTableSQL)
                db.execSQL("ALTER TABLE $VIEW_LIST_TABLE_NAME RENAME TO temp_record_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS `records` (`link` TEXT NOT NULL, `title` TEXT NOT NULL, `viewListType` INTEGER NOT NULL, `viewTime` INTEGER NOT NULL, PRIMARY KEY(`link`))")
                db.execSQL("INSERT INTO `records` (link, title, viewListType, viewTime) SELECT link, title, viewListType, datetime('now','localtime') FROM temp_record_table;")
                db.execSQL("DROP TABLE temp_record_table;")
                db.execSQL("ALTER TABLE $LIKE_AND_READ_TABLE_NAME RENAME TO temp_like_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS `like_table` (`link` TEXT NOT NULL, `title` TEXT NOT NULL, `like` INTEGER NOT NULL, `hasRead` INTEGER NOT NULL, PRIMARY KEY(`link`))")
                db.execSQL("INSERT INTO `like_table` (link, title, `like`, hasRead) SELECT link, title, `like`, hasRead FROM temp_like_table;")
                db.execSQL("DROP TABLE temp_like_table;")
            }
        }
        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `draft` (`draftId` INTEGER NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `lastModifyTime` INTEGER NOT NULL, PRIMARY KEY(`draftId`))")
            }
        }
        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `like_table` ADD COLUMN `boxId` INTEGER NOT NULL DEFAULT 0;")
                db.execSQL("CREATE TABLE IF NOT EXISTS `like_box_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
            }
        }

        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Room runs upgrades in a transaction. Read before rewriting either table
                // so that changing a primary key cannot overwrite an unprocessed row.
                val records = linkedMapOf<String, RecordV8>()
                var recordsChanged = false
                db.query("""
                    SELECT link, title, viewListType, viewTime,
                        CASE WHEN typeof(viewTime) = 'text'
                            THEN strftime('%s', viewTime, 'utc') * 1000 END
                    FROM records ORDER BY rowid
                """.trimIndent()).use { rows ->
                    while (rows.moveToNext()) {
                        val original = rows.getString(0)
                        val link = normalizeLinkForV8(original)
                        // The 4→5 migration stored local datetime text in this INTEGER column.
                        val legacyTime = rows.getType(3) == android.database.Cursor.FIELD_TYPE_STRING
                        val time = if (legacyTime && !rows.isNull(4)) rows.getLong(4) else rows.getLong(3)
                        val row = RecordV8(rows.getString(1), rows.getInt(2), time)
                        records[link] = records[link]?.merge(row) ?: row
                        recordsChanged = recordsChanged || link != original || legacyTime
                    }
                }
                if (recordsChanged) {
                    db.execSQL("DELETE FROM records")
                    records.forEach { (link, row) ->
                        db.execSQL(
                            "INSERT INTO records (link, title, viewListType, viewTime) VALUES (?, ?, ?, ?)",
                            arrayOf<Any>(link, row.title, row.type, row.time)
                        )
                    }
                }

                val boxIds = mutableSetOf<Int>()
                db.query("SELECT id FROM like_box_table").use { rows ->
                    while (rows.moveToNext()) boxIds.add(rows.getInt(0))
                }
                val likes = linkedMapOf<String, LikeV8>()
                var likesChanged = false
                db.query("SELECT link, title, `like`, hasRead, boxId FROM like_table ORDER BY rowid").use { rows ->
                    while (rows.moveToNext()) {
                        val original = rows.getString(0)
                        val link = normalizeLinkForV8(original)
                        val row = LikeV8(
                            rows.getString(1), rows.getInt(2) != 0, rows.getInt(3) != 0, rows.getInt(4)
                        )
                        likes[link] = likes[link]?.merge(row, boxIds) ?: row
                        likesChanged = likesChanged || link != original
                    }
                }
                if (likesChanged) {
                    db.execSQL("DELETE FROM like_table")
                    likes.forEach { (link, row) ->
                        db.execSQL(
                            "INSERT INTO like_table (link, title, `like`, hasRead, boxId) VALUES (?, ?, ?, ?, ?)",
                            arrayOf<Any>(link, row.title, if (row.liked) 1 else 0, if (row.read) 1 else 0, row.boxId)
                        )
                    }
                }
            }
        }

        fun getInstance(): AppInfoDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, AppInfoDatabase::class.java,
                        INFO_DB_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)
                        .addMigrations(MIGRATION_4_5)
                        .addMigrations(MIGRATION_5_6)
                        .addMigrations(MIGRATION_6_7)
                        .addMigrations(MIGRATION_7_8)
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }

        fun getNewInstance() {
            INSTANCE?.close()
            try {
                INSTANCE = null
                getInstance()
            } catch (e1: SQLiteDatabaseCorruptException) {
                Toast.makeText(ScpApplication.context, "创建数据库出错，请重试或检查版本", Toast.LENGTH_SHORT).show()
            } catch (e2: IllegalStateException) {
                Toast.makeText(ScpApplication.context, "创建数据库出错，请重试或检查版本", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
