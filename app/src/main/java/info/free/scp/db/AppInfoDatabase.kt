package info.free.scp.db

import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpInfoModel
import info.free.scp.bean.ScpReadModel


@Database(entities = [ScpInfoModel::class, ScpReadModel::class], version = 5)
abstract class AppInfoDatabase : RoomDatabase() {
    abstract fun likeAndReadDao(): LikeAndReadDao
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: AppInfoDatabase? = null

        private val MIGRATION_4_5: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        fun getInstance(): AppInfoDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, AppInfoDatabase::class.java,
                        SCP_DB_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }
    }
}