package info.free.scp.ui.user

import org.junit.Assert.assertEquals
import org.junit.Test

class EntryGridLayoutTest {
    @Test
    fun sevenEntriesLastSpansTwoColumns() {
        assertEquals(listOf(listOf(1, 1, 1, 1), listOf(1, 1, 2)), entryRowSpans(7))
    }

    @Test
    fun eightEntriesFillTwoFullRows() {
        assertEquals(listOf(listOf(1, 1, 1, 1), listOf(1, 1, 1, 1)), entryRowSpans(8))
    }

    @Test
    fun otherCounts() {
        assertEquals(emptyList<List<Int>>(), entryRowSpans(0))
        assertEquals(listOf(listOf(1, 1, 1, 1)), entryRowSpans(4))
        assertEquals(listOf(listOf(1, 1, 1, 1), listOf(4)), entryRowSpans(5))
        assertEquals(listOf(listOf(1, 1, 1, 1), listOf(1, 3)), entryRowSpans(6))
        assertEquals(listOf(listOf(1, 2)), entryRowSpans(2, columns = 3))
    }
}
