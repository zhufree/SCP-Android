package info.free.scp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpItemModel
import info.free.scp.data.update.PreserveDatabaseOnCorruption

@Database(entities = [ScpItemModel::class], version = 2)
abstract class ScpDatabase : RoomDatabase() {
    abstract fun scpDao(): ScpDao

    companion object {
        private var instance: ScpDatabase? = null
        @Volatile internal var ready = false

        @Synchronized
        fun getInstance(): ScpDatabase? {
            if (!ready) return null
            return instance ?: builder(ScpApplication.context, SCP_DB_NAME).build().also { instance = it }
        }

        internal fun builder(context: Context, name: String): Builder<ScpDatabase> =
            Room.databaseBuilder(context, ScpDatabase::class.java, name)
                .openHelperFactory(PreserveDatabaseOnCorruption)
                .allowMainThreadQueries()
    }
}
