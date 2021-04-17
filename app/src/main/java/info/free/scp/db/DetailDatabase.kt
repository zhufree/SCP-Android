package info.free.scp.db

import android.database.sqlite.SQLiteDatabaseCorruptException
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpDetail
import info.free.scp.bean.ScpItemModel
import info.free.scp.util.FileUtil
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.toast


@Database(entities = [ScpDetail::class], version = 1)
abstract class DetailDatabase : RoomDatabase() {
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: DetailDatabase? = null

        fun getInstance(): DetailDatabase? {
            try {
                if (INSTANCE == null && FileUtil.checkDataReady(DETAIL_DB_NAME)) {
                    INSTANCE = Room.databaseBuilder(ScpApplication.context, DetailDatabase::class.java,
                            DETAIL_DB_NAME)
                            .allowMainThreadQueries()
                            .build()
                    PreferenceUtil.setAppMode(OFFLINE)
                }
            } catch (e: SQLiteDatabaseCorruptException) {
                ScpApplication.currentActivity?.toast("创建数据库出错，请重试")
            }
            return INSTANCE
        }
    }
}