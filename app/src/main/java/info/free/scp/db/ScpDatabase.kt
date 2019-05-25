package info.free.scp.db

import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpDetail
import info.free.scp.bean.ScpItemModel
import info.free.scp.util.FileHelper


@Database(entities = [ScpItemModel::class, ScpDetail::class, ScpCollectionModel::class], version = 1)
abstract class ScpDatabase : RoomDatabase() {
    abstract fun scpDao(): ScpDao
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: ScpDatabase? = null

        fun getInstance(): ScpDatabase? {
            if (INSTANCE == null && FileHelper.getInstance(ScpApplication.context).checkDataExist()) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                        SCP_DB_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE
        }

        fun getNewInstance() {
            INSTANCE?.close()
            if (FileHelper.getInstance(ScpApplication.context).checkDataExist()) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                        SCP_DB_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
        }
    }
}