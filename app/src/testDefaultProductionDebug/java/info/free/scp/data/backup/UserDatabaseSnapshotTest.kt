package info.free.scp.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.sql.DriverManager

class UserDatabaseSnapshotTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun vacuumSnapshotIncludesCommittedRowsStillInTheWal() {
        val source = temp.newFile("source.db")
        val snapshot = File(source.parentFile, "snapshot.db")
        DriverManager.getConnection("jdbc:sqlite:$source").use { db ->
            db.createStatement().use { sql ->
                sql.execute("PRAGMA journal_mode = WAL")
                sql.execute("PRAGMA wal_autocheckpoint = 0")
                sql.execute("CREATE TABLE draft (content TEXT)")
                sql.execute("PRAGMA wal_checkpoint(TRUNCATE)")
                sql.execute("INSERT INTO draft VALUES ('committed only to WAL')")
            }
            assertTrue(File(source.path + "-wal").length() > 0)
            db.prepareStatement(VACUUM_SNAPSHOT_SQL).use {
                it.setString(1, snapshot.path)
                it.execute()
            }
            DriverManager.getConnection("jdbc:sqlite:file:$snapshot?mode=ro").use { copy ->
                copy.createStatement().use { sql ->
                    sql.executeQuery("SELECT content FROM draft").use {
                        assertTrue(it.next())
                        assertEquals("committed only to WAL", it.getString(1))
                    }
                }
            }
        }
    }
}
