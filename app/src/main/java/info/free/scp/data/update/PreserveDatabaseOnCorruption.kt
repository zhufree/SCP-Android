package info.free.scp.data.update

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

/** The cold-start installer owns quarantine and rollback. SQLite must leave the files in place. */
internal object PreserveDatabaseOnCorruption : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: Configuration): SupportSQLiteOpenHelper {
        val delegate = configuration.callback
        val callback = object : Callback(delegate.version) {
            override fun onConfigure(db: SupportSQLiteDatabase) = delegate.onConfigure(db)
            override fun onCreate(db: SupportSQLiteDatabase) = delegate.onCreate(db)
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                delegate.onUpgrade(db, oldVersion, newVersion)

            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                delegate.onDowngrade(db, oldVersion, newVersion)

            override fun onOpen(db: SupportSQLiteDatabase) = delegate.onOpen(db)
            override fun onCorruption(db: SupportSQLiteDatabase) = Unit
        }
        return FrameworkSQLiteOpenHelperFactory().create(
            Configuration.builder(configuration.context)
                .name(configuration.name)
                .noBackupDirectory(configuration.useNoBackupDirectory)
                .callback(callback)
                .build()
        )
    }
}

internal inline fun <T : RoomDatabase, R> T.useRoom(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        close()
    }
}
