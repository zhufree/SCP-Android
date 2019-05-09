package info.free.scp.db

import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpDetail


@Database(entities = [ScpDetail::class], version = 1)
abstract class DetailDatabase : RoomDatabase() {
    abstract fun detailDao(): DetailDao

    companion object {
        private var INSTANCE: DetailDatabase? = null


        fun getInstance(): DetailDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(ScpApplication.context, DetailDatabase::class.java,
                        DETAIL_DB_NAME)
                        .build()
            }
            return INSTANCE!!
        }
    }
}