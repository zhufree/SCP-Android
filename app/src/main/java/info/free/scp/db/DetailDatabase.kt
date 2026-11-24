package info.free.scp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpDetail
import info.free.scp.data.update.PreserveDatabaseOnCorruption

@Database(entities = [ScpDetail::class], version = 2)
abstract class DetailDatabase : RoomDatabase() {
    abstract fun detailDao(): DetailDao

    companion object {
        private var instance: DetailDatabase? = null
        @Volatile internal var ready = false

        @Synchronized
        fun getInstance(): DetailDatabase? {
            if (!ready) return null
            return instance ?: builder(ScpApplication.context, DETAIL_DB_NAME).build().also { instance = it }
        }

        internal fun builder(context: Context, name: String): Builder<DetailDatabase> =
            Room.databaseBuilder(context, DetailDatabase::class.java, name)
                .openHelperFactory(PreserveDatabaseOnCorruption)
                .allowMainThreadQueries()
    }
}
