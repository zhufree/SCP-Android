package info.free.scp.view.detail

import kotlin.math.roundToInt

/** 阅读进度换算。内容高度按整篇（WebView + 文末标签/评论）计，不是只看 WebView。 */
internal object ReadingScroll {
    /** 可滚动距离；内容不足一屏时为 0。 */
    fun range(contentHeight: Int, viewportHeight: Int): Int =
        (contentHeight - viewportHeight).coerceAtLeast(0)

    /** 0f..1f；不足一屏时整篇都在屏幕内，按已读完（1f）处理。 */
    fun progress(scrollY: Int, range: Int): Float =
        if (range <= 0) 1f else (scrollY.toFloat() / range).coerceIn(0f, 1f)

    fun percent(progress: Float): Int = (progress.coerceIn(0f, 1f) * 100).roundToInt()

    fun scrollOffset(progress: Float, range: Int): Int =
        (progress.coerceIn(0f, 1f) * range.coerceAtLeast(0)).roundToInt()
}

/** 快速滚动条滑块的几何换算，单位 px。 */
internal object FastScrollGeometry {
    fun thumbTop(progress: Float, trackHeight: Int, thumbHeight: Int): Float =
        progress.coerceIn(0f, 1f) * (trackHeight - thumbHeight).coerceAtLeast(0)

    /** 手指在轨道内的 y（相对轨道顶）换算成进度；grabOffset 是按下时手指到滑块顶的距离。 */
    fun progressAt(touchY: Float, grabOffset: Float, trackHeight: Int, thumbHeight: Int): Float {
        val travel = trackHeight - thumbHeight
        if (travel <= 0) return 0f
        return ((touchY - grabOffset) / travel).coerceIn(0f, 1f)
    }
}

/**
 * 顶栏/底栏的收起规则：向下累计滚动超过 [hideThresholdPx] 收起，向上累计超过 [showThresholdPx] 展开；
 * 在顶部、文末（距底 [edgeSlackPx] 以内）或内容不足一屏时始终展开。
 * 拖动快速滚动条时只收起不展开（到顶/到底除外），避免往回拖时两栏来回弹出。
 */
internal class ReadingBarsPolicy(
    private val hideThresholdPx: Int,
    private val showThresholdPx: Int,
    private val edgeSlackPx: Int
) {
    var expanded = true
        private set
    private var travel = 0

    fun reset() {
        expanded = true
        travel = 0
    }

    /** 返回新的展开状态；状态没变时返回 null。 */
    fun onScrolled(dy: Int, scrollY: Int, range: Int, dragging: Boolean = false): Boolean? {
        if (range <= 0 || scrollY <= 0 || range - scrollY <= edgeSlackPx) {
            travel = 0
            return change(true)
        }
        if (dy == 0) return null
        travel = if (dy > 0) (if (travel > 0) travel + dy else dy) else (if (travel < 0) travel + dy else dy)
        return when {
            travel >= hideThresholdPx -> change(false)
            travel <= -showThresholdPx && !dragging -> change(true)
            else -> null
        }
    }

    private fun change(target: Boolean): Boolean? {
        if (expanded == target) return null
        expanded = target
        return target
    }
}
