package info.free.scp.db

import org.junit.Assert.assertEquals
import org.junit.Test

class AppInfoMigrationV8Test {
    @Test
    fun recordKeepsTypeAndTitleOfLatestTimestamp() {
        val later = RecordV8("Later", 1, 200)
        val history = RecordV8("History", 0, 100)
        assertEquals(later, later.merge(history))
        assertEquals(later, history.merge(later))
    }

    @Test
    fun recordUsesHigherRowIdWhenTimestampsTie() {
        val history = RecordV8("History", 0, 200)
        assertEquals(history, RecordV8("Later", 1, 200).merge(history))
    }

    @Test
    fun likesKeepBothFlagsAndPreferAnExistingBoxOnALikedRow() {
        val liked = LikeV8("Liked", liked = true, read = false, boxId = 4)
        val read = LikeV8("Read", liked = false, read = true, boxId = 6)
        val newer = LikeV8("Newer", liked = true, read = false, boxId = 0)
        assertEquals(liked.copy(read = true), liked.merge(read, setOf(4, 6)).merge(newer, setOf(4, 6)))
    }

    @Test
    fun likesUseHigherRowIdWhenBothRowsHaveValidBoxes() {
        val first = LikeV8("First", liked = true, read = true, boxId = 4)
        val second = LikeV8("Second", liked = true, read = false, boxId = 6)
        assertEquals(second.copy(read = true), first.merge(second, setOf(4, 6)))
    }
}
