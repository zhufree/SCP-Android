package info.free.scp.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferProgressTest {
    @Test fun aggregatesByBytesAndDoesNotCountASecondAttemptTwice() {
        var now = 0L
        val snapshots = mutableListOf<TransferProgress>()
        val tracker = TransferTracker(TransferKind.ALL_PARTS, listOf(100L, 300L), { now }, snapshots::add)
        tracker.startPart(1, "first")
        now += 200
        tracker.onBytes(80)
        assertEquals(20, snapshots.last().percent)
        assertNull(snapshots.last().remainingSeconds)

        tracker.startAttempt(switchingSource = true)
        assertTrue(snapshots.last().switchingSource)
        now += 1_000
        tracker.onBytes(50)
        assertEquals(80L, snapshots.last().receivedBytes)
        assertEquals(50L, snapshots.last().bytesPerSecond)
        assertEquals(7L, snapshots.last().remainingSeconds)
        tracker.onBytes(100)
        tracker.verifying()
        assertEquals(99L, snapshots.last().receivedBytes)
        assertNull(snapshots.last().bytesPerSecond)
        tracker.verified()
        tracker.merging()
        assertEquals(TransferPhase.MERGING, snapshots.last().phase)

        tracker.startPart(2, "second")
        assertEquals(2, snapshots.last().partIndex)
        assertEquals(2, snapshots.last().partCount)
        assertEquals(100L, snapshots.last().receivedBytes)
        assertEquals(400L, snapshots.last().totalBytes)
        now += 1_000
        tracker.onBytes(150)
        assertEquals(62, snapshots.last().percent)
        tracker.onBytes(300)
        tracker.verified()
        assertEquals(100, snapshots.last().percent)
        assertTrue(snapshots.zipWithNext().all { (a, b) -> a.receivedBytes <= b.receivedBytes })
    }

    @Test fun continuingAfterFailedPartKeepsOffsetsAndDoesNotClaimCompletion() {
        var now = 0L
        val snapshots = mutableListOf<TransferProgress>()
        val tracker = TransferTracker(TransferKind.ALL_PARTS, listOf(100L, 300L), { now }, snapshots::add)
        tracker.startPart(1, "first")
        now += 50
        tracker.onBytes(20)
        tracker.startPart(2, "second")
        tracker.onBytes(300)
        tracker.verified()
        assertEquals(320L, snapshots.last().receivedBytes)
        assertEquals(80, snapshots.last().percent)
    }
}
