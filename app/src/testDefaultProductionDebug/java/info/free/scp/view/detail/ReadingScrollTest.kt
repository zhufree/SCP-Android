package info.free.scp.view.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingScrollTest {
    @Test
    fun rangeIsContentMinusViewportAndNeverNegative() {
        assertEquals(1200, ReadingScroll.range(contentHeight = 2000, viewportHeight = 800))
        assertEquals(0, ReadingScroll.range(contentHeight = 500, viewportHeight = 800))
    }

    @Test
    fun progressFollowsScrollOffsetOverWholeContentRange() {
        assertEquals(0f, ReadingScroll.progress(0, 1200), 0f)
        assertEquals(0.5f, ReadingScroll.progress(600, 1200), 0f)
        assertEquals(1f, ReadingScroll.progress(1200, 1200), 0f)
    }

    @Test
    fun progressIsClampedWhenOverscrolled() {
        assertEquals(0f, ReadingScroll.progress(-30, 1200), 0f)
        assertEquals(1f, ReadingScroll.progress(1500, 1200), 0f)
    }

    @Test
    fun contentShorterThanOneScreenCountsAsFullyRead() {
        assertEquals(1f, ReadingScroll.progress(0, 0), 0f)
    }

    @Test
    fun percentRoundsToNearestInteger() {
        assertEquals(0, ReadingScroll.percent(0f))
        assertEquals(62, ReadingScroll.percent(0.624f))
        assertEquals(63, ReadingScroll.percent(0.625f))
        assertEquals(100, ReadingScroll.percent(1.4f))
    }

    @Test
    fun scrollOffsetIsInverseOfProgress() {
        assertEquals(600, ReadingScroll.scrollOffset(0.5f, 1200))
        assertEquals(1200, ReadingScroll.scrollOffset(2f, 1200))
        assertEquals(0, ReadingScroll.scrollOffset(0.7f, 0))
    }
}

class FastScrollGeometryTest {
    @Test
    fun thumbTravelsWithinTrackMinusThumbHeight() {
        assertEquals(0f, FastScrollGeometry.thumbTop(0f, 800, 100), 0f)
        assertEquals(350f, FastScrollGeometry.thumbTop(0.5f, 800, 100), 0f)
        assertEquals(700f, FastScrollGeometry.thumbTop(1f, 800, 100), 0f)
    }

    @Test
    fun thumbStaysAtTopWhenTrackIsShorterThanThumb() {
        assertEquals(0f, FastScrollGeometry.thumbTop(0.8f, 80, 100), 0f)
    }

    @Test
    fun dragKeepsTheGrabbedPointUnderTheFinger() {
        // 按在滑块中部（离滑块顶 40px），手指移到 y=390 时滑块顶在 350，即进度 0.5
        assertEquals(0.5f, FastScrollGeometry.progressAt(390f, 40f, 800, 100), 0f)
    }

    @Test
    fun dragProgressIsClampedAtBothEnds() {
        assertEquals(0f, FastScrollGeometry.progressAt(-50f, 40f, 800, 100), 0f)
        assertEquals(1f, FastScrollGeometry.progressAt(900f, 40f, 800, 100), 0f)
        assertEquals(0f, FastScrollGeometry.progressAt(300f, 0f, 80, 100), 0f)
    }
}

class ReadingBarsPolicyTest {
    private fun policy() = ReadingBarsPolicy(hideThresholdPx = 24, showThresholdPx = 16, edgeSlackPx = 8)
    private val range = 5000

    @Test
    fun startsExpanded() {
        assertTrue(policy().expanded)
    }

    @Test
    fun smallDownwardScrollKeepsBarsAndLargeOneCollapsesThem() {
        val p = policy()
        assertNull(p.onScrolled(dy = 10, scrollY = 400, range = range))
        assertEquals(false, p.onScrolled(dy = 20, scrollY = 420, range = range))
        assertNull(p.onScrolled(dy = 50, scrollY = 470, range = range))
    }

    @Test
    fun scrollingUpExpandsAgainAfterThreshold() {
        val p = policy()
        p.onScrolled(dy = 40, scrollY = 400, range = range)
        assertNull(p.onScrolled(dy = -10, scrollY = 390, range = range))
        assertEquals(true, p.onScrolled(dy = -10, scrollY = 380, range = range))
    }

    @Test
    fun changingDirectionResetsAccumulatedDistance() {
        val p = policy()
        assertNull(p.onScrolled(dy = 20, scrollY = 400, range = range))
        assertNull(p.onScrolled(dy = -5, scrollY = 395, range = range))
        assertNull(p.onScrolled(dy = 20, scrollY = 415, range = range))
        assertEquals(false, p.onScrolled(dy = 10, scrollY = 425, range = range))
    }

    @Test
    fun alwaysExpandedAtTop() {
        val p = policy()
        p.onScrolled(dy = 40, scrollY = 400, range = range)
        assertEquals(true, p.onScrolled(dy = -400, scrollY = 0, range = range))
    }

    @Test
    fun expandsWhenReachingTheEndOfTheArticle() {
        val p = policy()
        p.onScrolled(dy = 40, scrollY = 400, range = range)
        assertEquals(true, p.onScrolled(dy = 200, scrollY = range - 4, range = range))
    }

    @Test
    fun shortContentKeepsBarsExpanded() {
        val p = policy()
        assertNull(p.onScrolled(dy = 100, scrollY = 100, range = 0))
        assertTrue(p.expanded)
    }

    @Test
    fun draggingTheScrollbarOnlyCollapses() {
        val p = policy()
        assertEquals(false, p.onScrolled(dy = 300, scrollY = 1000, range = range, dragging = true))
        assertNull(p.onScrolled(dy = -300, scrollY = 700, range = range, dragging = true))
        assertEquals(false, p.expanded)
        assertEquals(true, p.onScrolled(dy = -700, scrollY = 0, range = range, dragging = true))
    }

    @Test
    fun resetExpandsAndForgetsTravel() {
        val p = policy()
        p.onScrolled(dy = 40, scrollY = 400, range = range)
        p.reset()
        assertTrue(p.expanded)
        assertNull(p.onScrolled(dy = 10, scrollY = 10 + 400, range = range))
    }
}
