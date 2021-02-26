package info.free.scp.db

import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpDetail
import info.free.scp.bean.ScpItemModel
import info.free.scp.util.FileUtil
import org.jetbrains.anko.toast


@Database(entities = [ScpItemModel::class, ScpDetail::class, ScpCollectionModel::class], version = 1)
abstract class ScpDatabase : RoomDatabase() {
    abstract fun scpDao(): ScpDao
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: ScpDatabase? = null

        fun getInstance(): ScpDatabase {
            try {
                if (INSTANCE == null && FileUtil.getInstance(ScpApplication.context).checkDataReady()) {
                    INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                            SCP_DB_NAME)
                            .allowMainThreadQueries()
                            .build()
                }
            } catch (e: SQLiteDatabaseCorruptException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试")
            }
            return INSTANCE!!
        }

        fun getNewInstance() {
            INSTANCE?.close()
            try {
                if (FileUtil.getInstance(ScpApplication.context).checkDataReady()) {
                    INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                            SCP_DB_NAME)
                            .allowMainThreadQueries()
                            .build()
                }
            } catch (e: SQLiteDatabaseCorruptException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试")
            }
        }
    }
}