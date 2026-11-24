package info.free.scp.data.update

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class PartMergeSqlTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun attachesLocalFromPartAndMergesSnapshotIdempotently() {
        val (part, local) = fixture()
        connect(part).use { source ->
            attach(source, local)
            val sql = PartMergeSql("local")
            for (attempt in 1..2) {
                source.runSql("BEGIN IMMEDIATE")
                mergeAttached(source, sql)
                source.runSql("COMMIT")
            }
            source.runSql("DETACH DATABASE local")
            assertEquals(listOf("/new:0", "/shared:1"), source.values("SELECT link || ':' || not_found FROM scp_detail ORDER BY link"))
            assertFalse(source.values("SELECT name FROM sqlite_master").contains("x_part_state"))
        }
        connect(local).use { target ->
            assertEquals(listOf("/legacy:0", "/new:0", "/other:0", "/shared:1"),
                target.values("SELECT link || ':' || not_found FROM scp_detail ORDER BY link"))
            assertEquals(listOf("/new", "/shared"),
                target.values("SELECT link FROM x_detail_part WHERE part_id = 'selected' ORDER BY link"))
            assertEquals(listOf("8:selected-digest:2"),
                target.values("SELECT parts_version || ':' || digest || ':' || rows FROM x_part_state WHERE part_id = 'selected'"))
            assertEquals(listOf("2"), target.values("PRAGMA user_version"))
        }
    }

    @Test fun retiresOnlyRowsStillOwnedByRetiredPart() {
        val (part, local) = fixture()
        connect(part).use { source ->
            attach(source, local)
            val sql = PartMergeSql("local")
            source.runSql("BEGIN IMMEDIATE")
            sql.removeRows().forEach { source.runSql(it, "retired") }
            source.runSql(sql.deleteState, "retired")
            mergeAttached(source, sql)
            source.runSql("COMMIT")
            source.runSql("DETACH DATABASE local")
        }
        connect(local).use { target ->
            assertEquals(listOf("/legacy", "/new", "/shared"),
                target.values("SELECT link FROM scp_detail ORDER BY link"))
            assertEquals(listOf("selected"), target.values("SELECT part_id FROM x_part_state"))
        }
    }

    @Test fun rollsBackUpsertsDeletesAndOwnershipOnStateWriteFailure() {
        val (part, local) = fixture()
        connect(local).use {
            it.runSql("""
                CREATE TRIGGER reject_state BEFORE INSERT ON x_part_state
                BEGIN SELECT RAISE(ABORT, 'fixture rejects state'); END
            """.trimIndent())
        }
        connect(part).use { source ->
            attach(source, local)
            val sql = PartMergeSql("local")
            source.runSql("BEGIN IMMEDIATE")
            assertThrows(SQLException::class.java) { mergeAttached(source, sql) }
            source.runSql("ROLLBACK")
            source.runSql("DETACH DATABASE local")
        }
        connect(local).use { target ->
            assertEquals(listOf("/legacy:0", "/old:0", "/other:0", "/shared:0"),
                target.values("SELECT link || ':' || not_found FROM scp_detail ORDER BY link"))
            assertEquals(listOf("/old:selected", "/other:retired", "/shared:retired"),
                target.values("SELECT link || ':' || part_id FROM x_detail_part ORDER BY link"))
            assertEquals(listOf("retired"), target.values("SELECT part_id FROM x_part_state"))
        }
    }

    @Test fun cursorFallbackSqlUsesTemporarySnapshotAndPreservesNulls() {
        val (_, local) = fixture()
        connect(local).use { target ->
            val sql = PartMergeSql("main")
            target.runSql("BEGIN IMMEDIATE")
            target.runSql("CREATE TEMP TABLE part_snapshot (link TEXT NOT NULL PRIMARY KEY)")
            target.runSql(sql.upsertRow, "/new", null, 1, null)
            target.runSql("INSERT INTO temp.part_snapshot VALUES (?)", "/new")
            sql.removeRows("temp.part_snapshot").forEach { target.runSql(it, "selected") }
            target.runSql(sql.recordLinks("temp.part_snapshot"), "selected")
            target.runSql("DROP TABLE temp.part_snapshot")
            target.runSql("COMMIT")
            assertEquals(listOf("/legacy", "/new", "/other", "/shared"),
                target.values("SELECT link FROM scp_detail ORDER BY link"))
            assertEquals(listOf("/new"), target.values("SELECT link FROM x_detail_part WHERE part_id = 'selected'"))
            assertEquals(listOf("1:1:1"), target.values(
                "SELECT (detail IS NULL) || ':' || not_found || ':' || (tags IS NULL) FROM scp_detail WHERE link = '/new'"
            ))
        }
    }

    private fun fixture(): Pair<File, File> {
        val part = temp.newFile("part.db")
        val local = temp.newFile("detail.db")
        val detailTable = "CREATE TABLE scp_detail (link TEXT NOT NULL PRIMARY KEY, detail TEXT, not_found INTEGER, tags TEXT)"
        connect(part).use {
            it.runSql(detailTable)
            it.runSql("INSERT INTO scp_detail VALUES ('/new','html',0,'tag'),('/shared',NULL,1,NULL)")
        }
        connect(local).use {
            it.runSql(detailTable)
            it.runSql("PRAGMA user_version=2")
            assertEquals(listOf("wal"), it.values("PRAGMA journal_mode=WAL"))
            val sql = PartMergeSql("main")
            it.runSql(sql.createOwnership)
            it.runSql(sql.createState)
            it.runSql("""
                INSERT INTO scp_detail VALUES
                ('/legacy','html',0,NULL),('/old','html',0,NULL),('/other','html',0,NULL),('/shared','old html',0,NULL)
            """.trimIndent())
            it.runSql("INSERT INTO x_detail_part VALUES ('/old','selected'),('/other','retired'),('/shared','retired')")
            it.runSql(sql.insertState, "retired", "7", "old-digest", "old-export", "sha", 2, 0)
        }
        return part to local
    }

    private fun attach(source: Connection, local: File) =
        source.runSql("ATTACH DATABASE ? AS local", local.absolutePath)

    private fun mergeAttached(source: Connection, sql: PartMergeSql) {
        source.runSql(sql.createOwnership)
        source.runSql(sql.createState)
        source.runSql(sql.upsertFromPart)
        sql.removeRows("main.scp_detail").forEach { source.runSql(it, "selected") }
        source.runSql(sql.recordLinks("main.scp_detail"), "selected")
        source.runSql(sql.insertState, "selected", "8", "selected-digest", "export", "sha", 2, 123)
    }

    private fun connect(file: File): Connection = DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")

    private fun Connection.runSql(sql: String, vararg args: Any?) {
        prepareStatement(sql).use { statement ->
            args.forEachIndexed { index, value -> statement.setObject(index + 1, value) }
            statement.execute()
        }
    }

    private fun Connection.values(sql: String): List<String> = createStatement().use { statement ->
        statement.executeQuery(sql).use { rows ->
            buildList { while (rows.next()) add(rows.getString(1)) }
        }
    }
}
