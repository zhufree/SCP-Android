package info.free.scp.data.detail

import info.free.scp.bean.ScpDetail
import info.free.scp.db.LEGACY_DETAIL_LOOKUP
import org.junit.Assert.assertEquals
import org.junit.Test
import java.sql.DriverManager

class DetailReadResultTest {
    @Test fun classifiesHitMissingNotFoundAndReadFailure() {
        assertEquals(DetailReadResult.Hit("body", "tag"), classifyDetail(ScpDetail("/a", "body", 0, "tag")))
        assertEquals(DetailReadResult.Missing, classifyDetail(null))
        assertEquals(DetailReadResult.Missing, classifyDetail(ScpDetail("/b", "null", 0)))
        assertEquals(DetailReadResult.NotFound, classifyDetail(ScpDetail("/c", null, 1)))
        assertEquals(DetailReadResult.NotFound, classifyDetail(ScpDetail("/c", null, 404)))
        assertEquals(DetailReadResult.Failed, classifyDetail(null, IllegalStateException("read failed")))
    }

    @Test fun legacyKeysResolveAlongsideNewCanonicalRows() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { db ->
            db.createStatement().use {
                it.execute("CREATE TABLE scp_detail (link TEXT NOT NULL PRIMARY KEY, detail TEXT, not_found INTEGER, tags TEXT)")
                it.execute("INSERT INTO scp_detail VALUES ('/SCP-1#pages', 'old', 0, 'legacy'), ('/scp-2', 'new', 0, 'part')")
            }
            db.prepareStatement(LEGACY_DETAIL_LOOKUP).use {
                it.setString(1, "/scp-1")
                it.executeQuery().use { row ->
                    row.next()
                    assertEquals("old", row.getString("detail"))
                }
                it.setString(1, "/scp-2")
                it.executeQuery().use { row ->
                    row.next()
                    assertEquals("new", row.getString("detail"))
                }
            }
        }
    }
}
