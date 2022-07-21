package info.free.scp.db

import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpItemModel
import org.jetbrains.anko.toast


@Database(entities = [ScpItemModel::class], version = 2)
abstract class ScpDatabase : RoomDatabase() {
    abstract fun scpDao(): ScpDao

    companion object {
        private var INSTANCE: ScpDatabase? = null

        fun getInstance(): ScpDatabase? {
            try {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                            SCP_DB_NAME)
                        .createFromAsset(SCP_DB_NAME)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            } catch (e1: SQLiteDatabaseCorruptException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试或检查版本")
            } catch (e2: IllegalStateException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试或检查版本")
            }
            return INSTANCE
        }

        fun close() {
            INSTANCE?.close()
        }

        fun getNewInstance() {
            INSTANCE?.close()
            try {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                        SCP_DB_NAME)
                    .createFromAsset(SCP_DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            } catch (e1: SQLiteDatabaseCorruptException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试或检查版本")
            } catch (e2: IllegalStateException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试或检查版本")
            }
        }
    }
}