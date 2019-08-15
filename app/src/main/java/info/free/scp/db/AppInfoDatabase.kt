package info.free.scp.db

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


@Database(entities = [ScpRecordModel::class, ScpLikeModel::class, DraftModel::class, ScpLikeBox::class], version = 7)
@TypeConverters(Converters::class)
abstract class AppInfoDatabase : RoomDatabase() {
    abstract fun likeAndReadDao(): LikeAndReadDao
    abstract fun readRecordDao(): ReadRecordDao
    abstract fun draftDao(): DraftDao

    companion object {
        private var INSTANCE: AppInfoDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 升级时删掉数据部分，只留info部分
                database.execSQL(dropDetailTableSQL)
                database.execSQL(dropScpTableSQL)
                database.execSQL("ALTER TABLE $VIEW_LIST_TABLE_NAME RENAME TO temp_record_table;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `records` (`link` TEXT NOT NULL, `title` TEXT NOT NULL, `viewListType` INTEGER NOT NULL, `viewTime` INTEGER NOT NULL, PRIMARY KEY(`link`))")
                database.execSQL("INSERT INTO `records` (link, title, viewListType, viewTime) SELECT link, title, viewListType, datetime('now','localtime') FROM temp_record_table;")
                database.execSQL("DROP TABLE temp_record_table;")
                database.execSQL("ALTER TABLE $LIKE_AND_READ_TABLE_NAME RENAME TO temp_like_table;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `like_table` (`link` TEXT NOT NULL, `title` TEXT NOT NULL, `like` INTEGER NOT NULL, `hasRead` INTEGER NOT NULL, PRIMARY KEY(`link`))")
                database.execSQL("INSERT INTO `like_table` (link, title, `like`, hasRead) SELECT link, title, `like`, hasRead FROM temp_like_table;")
                database.execSQL("DROP TABLE temp_like_table;")
            }
        }
        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `draft` (`draftId` INTEGER NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `lastModifyTime` INTEGER NOT NULL, PRIMARY KEY(`draftId`))")
            }
        }
        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `like_table` ADD COLUMN `boxId` INTEGER;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `like_box_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
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
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }
    }
}