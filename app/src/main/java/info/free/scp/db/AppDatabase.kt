package info.free.scp.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Room
import info.free.scp.SCPConstants.DB_NAME
import info.free.scp.bean.ScpModel


@Database(entities = [ScpModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scpDao(): ArticleDao

    companion object {
        private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                        DB_NAME)
                        .build()
            }
            return INSTANCE!!
        }
    }
}