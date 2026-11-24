package info.free.scp.data.update

import info.free.scp.data.backup.requiredUserTables
import info.free.scp.db.APP_INFO_VERSION
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.RandomAccessFile
import java.sql.DriverManager

class DatabasePreflightTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun acceptsAUserDatabaseWithAllRequiredTables() {
        val file = userDatabase()
        validateUser(file)
    }

    @Test fun rejectsAnEmptyUserDatabaseBeforeSqliteCanInitializeIt() {
        assertThrows(IllegalArgumentException::class.java) { validateUser(temp.newFile()) }
    }

    @Test fun rejectsATruncatedUserDatabase() {
        val file = userDatabase()
        RandomAccessFile(file, "rw").use { it.setLength(128) }
        assertThrows(Exception::class.java) { validateUser(file) }
    }

    @Test fun rejectsAUserDatabaseWithANewerSchema() {
        val failure = assertThrows(IllegalArgumentException::class.java) { validateUser(userDatabase(version = 9)) }
        assertTrue(failure.message.orEmpty().contains("9"))
    }

    @Test fun rejectsAUserDatabaseThatIsMissingADraftTable() {
        val failure = assertThrows(IllegalArgumentException::class.java) { validateUser(userDatabase(missing = "draft")) }
        assertTrue(failure.message.orEmpty().contains("draft"))
    }

    @Test fun catalogQuarantinesAnIncompatibleFileAndCanRetryAFailedAssetCopy() {
        val live = temp.newFile("catalog.db")
        DriverManager.getConnection("jdbc:sqlite:$live").use { db ->
            db.createStatement().use { it.execute("CREATE TABLE scps (obsolete TEXT)") }
        }
        val old = live.readBytes()
        val quarantined = File(live.path + ".corrupt")
        val probe: (File) -> Unit = { file ->
            requireNonEmpty(file)
            DriverManager.getConnection("jdbc:sqlite:file:$file?mode=ro").use { db ->
                db.createStatement().use { sql ->
                    sql.executeQuery("SELECT link FROM scps").use { check(it.next()) }
                }
            }
        }
        assertThrows(IllegalStateException::class.java) { recoverCatalog(live, probe) { error("out of space") } }
        assertFalse(live.exists())
        assertArrayEquals(old, quarantined.readBytes())
        recoverCatalog(live, probe) {
            DriverManager.getConnection("jdbc:sqlite:$live").use { db ->
                db.createStatement().use {
                    it.execute("CREATE TABLE scps (link TEXT)")
                    it.execute("INSERT INTO scps VALUES ('/scp-1')")
                }
            }
        }
        assertFalse(recoverCatalog(live, probe) { error("healthy catalog must stay in use") })
        assertArrayEquals(old, quarantined.readBytes())
    }

    @Test fun rejectsEmptyOrUnreadableFullDetailDatabase() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { db ->
            db.createStatement().use { sql ->
                sql.execute("CREATE TABLE scp_detail (link TEXT PRIMARY KEY, detail TEXT, not_found INTEGER, tags TEXT)")
                fun probe() = sql.executeQuery(VALID_DETAIL_PROBE).use { requireReadableDetail(it.next()) }
                assertThrows(IllegalArgumentException::class.java, ::probe)
                sql.execute("INSERT INTO scp_detail VALUES ('/missing', NULL, 1, NULL), ('/bad', X'0102', 0, NULL)")
                assertThrows(IllegalArgumentException::class.java, ::probe)
                sql.execute("INSERT INTO scp_detail VALUES ('/good', 'real body', 0, 'tag')")
                probe()
            }
        }
    }

    private fun userDatabase(version: Int = APP_INFO_VERSION, missing: String = ""): File = temp.newFile().also { file ->
        DriverManager.getConnection("jdbc:sqlite:$file").use { db ->
            db.createStatement().use { sql ->
                requiredUserTables(version).filterNot { it == missing }.forEach { sql.execute("CREATE TABLE $it (id INTEGER)") }
                sql.execute("PRAGMA user_version = $version")
            }
        }
    }

    private fun validateUser(file: File) {
        requireNonEmpty(file)
        DriverManager.getConnection("jdbc:sqlite:file:$file?mode=ro").use { db ->
            db.createStatement().use { sql ->
                validateDatabase(file, APP_INFO_VERSION, ::requiredUserTables,
                    scalar = { query -> sql.executeQuery(query).use { it.next(); it.getString(1) } },
                    hasTable = { table ->
                        db.prepareStatement("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?").use {
                            it.setString(1, table)
                            it.executeQuery().use { rows -> rows.next() }
                        }
                    })
            }
        }
    }
}
