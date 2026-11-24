package info.free.scp.data.catalog

import info.free.scp.bean.ScpModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentListKeysTest {
    @Test fun catalogPreservesDistinctRowIdsForTheSameArticle() {
        val rows = listOf(ScpModel(id = 15, link = "/scp-1"), ScpModel(id = 20, link = "/scp-1"))
        assertEquals(listOf(15, 20), rows.map(::catalogRowKey))
        assertEquals(listOf(rows.first()), rows.distinctDocuments())
    }

    @Test fun searchKeepsTheFirstCanonicalMatchInOrder() {
        val rows = listOf(
            ScpModel(id = 1, link = "/SCP-1#pages"),
            ScpModel(id = 2, link = "/scp-2"),
            ScpModel(id = 3, link = "https://scp-wiki-cn.wikidot.com/scp-1/")
        )
        assertEquals(rows.take(2), rows.distinctDocuments())
    }
}
