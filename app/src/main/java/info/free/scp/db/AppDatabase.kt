package info.free.scp.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import info.free.scp.SCPConstants.DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpDetail
import info.free.scp.bean.ScpModel


@Database(entities = [ScpModel::class, ScpDetail::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scpDao(): ScpDao
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: AppDatabase? = null


        fun getInstance(): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, AppDatabase::class.java,
                        DB_NAME)
                        .build()
            }
            return INSTANCE!!
        }
    }
}