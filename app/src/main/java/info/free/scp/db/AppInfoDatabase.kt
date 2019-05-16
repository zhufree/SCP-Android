package info.free.scp.db

import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpRecordModel
import info.free.scp.bean.ScpLikeModel
import info.free.scp.db.ScpTable.dropDetailTableSQL
import info.free.scp.db.ScpTable.dropScpTableSQL


@Database(entities = [ScpRecordModel::class, ScpLikeModel::class], version = 5)
abstract class AppInfoDatabase : RoomDatabase() {
    abstract fun likeAndReadDao(): LikeAndReadDao
    abstract fun readRecordDao(): ReadRecordDao

    companion object {
        private var INSTANCE: AppInfoDatabase? = null

        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 升级时删掉数据部分，只留info部分
                database.execSQL(dropDetailTableSQL)
                database.execSQL(dropScpTableSQL)
            }
        }

        fun getInstance(): AppInfoDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, AppInfoDatabase::class.java,
                        INFO_DB_NAME)
                        .addMigrations(MIGRATION_4_5)
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }
    }
}