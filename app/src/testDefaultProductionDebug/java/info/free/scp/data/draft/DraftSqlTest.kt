package info.free.scp.data.draft

import org.junit.Assert.assertEquals
import org.junit.Test
import java.sql.DriverManager

class DraftSqlTest {
    @Test fun sqliteAllocatesAfterHighestSurvivingDraft() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { db ->
            db.createStatement().use { sql ->
                sql.execute("CREATE TABLE draft (draftId INTEGER PRIMARY KEY, lastModifyTime INTEGER NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL)")
                sql.execute("INSERT INTO draft VALUES (1, 0, 'first', ''), (2, 0, 'second', ''), (3, 0, 'third', '')")
                sql.execute("DELETE FROM draft WHERE draftId = 2")
            }
            db.prepareStatement(INSERT_DRAFT_SQL).use {
                it.setLong(1, 100)
                it.setString(2, "new")
                it.setString(3, "body")
                assertEquals(1, it.executeUpdate())
                it.setString(2, "another")
                assertEquals(1, it.executeUpdate())
            }
            db.createStatement().use { sql ->
                sql.executeQuery("SELECT draftId, title FROM draft ORDER BY draftId").use { rows ->
                    val drafts = buildList { while (rows.next()) add(rows.getInt(1) to rows.getString(2)) }
                    assertEquals(listOf(1 to "first", 3 to "third", 4 to "new", 5 to "another"), drafts)
                }
            }
        }
    }
}
