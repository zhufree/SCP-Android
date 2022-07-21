package info.free.scp.db

import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpDetail
import info.free.scp.util.FileUtil
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.toast


@Database(entities = [ScpDetail::class], version = 2)
abstract class DetailDatabase : RoomDatabase() {
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: DetailDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS scp_detail;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `scp_detail` (`link` TEXT NOT NULL, `detail` TEXT, `not_found` INTEGER, `tags` TEXT, PRIMARY KEY(`link`));")
            }
        }

        fun getInstance(): DetailDatabase? {
            try {
                if (INSTANCE == null && FileUtil.checkDataReady(DETAIL_DB_NAME)) {
                    INSTANCE = Room.databaseBuilder(ScpApplication.context, DetailDatabase::class.java,
                            DETAIL_DB_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                    PreferenceUtil.setAppMode(OFFLINE)
                }
            } catch (e1: SQLiteDatabaseCorruptException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试或检查版本")
            } catch (e2: IllegalStateException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试或检查版本")
            }
            return INSTANCE
        }
    }
}