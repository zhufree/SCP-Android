package info.free.scp.db

import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpDetail
import info.free.scp.bean.ScpModel


@Database(entities = [ScpModel::class, ScpDetail::class], version = 1)
abstract class ScpDatabase : RoomDatabase() {
    abstract fun scpDao(): ScpDao
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: ScpDatabase? = null


        fun getInstance(): ScpDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, ScpDatabase::class.java,
                        SCP_DB_NAME)
                        .allowMainThreadQueries()
                        .build()
            }
            return INSTANCE!!
        }
    }
}