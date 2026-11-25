package info.free.scp.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedItemFormatTest {
    @Test
    fun scpNumberPrefixLength() {
        assertEquals("SCP-CN-3017".length, scpNumberLength("SCP-CN-3017 - 雨停之后"))
        assertEquals("SCP-8821".length, scpNumberLength("SCP-8821 逆行的钟摆"))
        assertEquals("scp-173".length, scpNumberLength("scp-173"))
        assertEquals("SCP-CN-001-J".length, scpNumberLength("SCP-CN-001-J 测试"))
        assertEquals(0, scpNumberLength("夜班守则 - 谁在敲门"))
        assertEquals(0, scpNumberLength(""))
    }

    @Test
    fun positiveRank() {
        assertTrue(isPositiveRank("+18"))
        assertTrue(isPositiveRank("5"))
        assertFalse(isPositiveRank("0"))
        assertFalse(isPositiveRank("-3"))
        assertFalse(isPositiveRank(""))
        assertFalse(isPositiveRank("n/a"))
    }
}
