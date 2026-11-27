package info.free.scp.view.detail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.color.MaterialColors
import info.free.scp.util.Utils

/**
 * 叠在 [NestedScrollView] 右缘的快速滚动条：静止时隐藏，滚动时出现；按住滑块拖动时变粗并显示百分比，
 * 松手约 1.5 秒后淡出。只有滑块可见时才拦截触摸，并向系统申请手势排除区，避免和侧滑返回冲突。
 * 滚动事件由宿主通过 [onContentScrolled] 转进来。
 */
class FastScrollView(context: Context) : View(context) {
    private var scrollView: NestedScrollView? = null
    var isDragging = false
        private set
    private var shown = false
    private var grabOffset = 0f

    private val thumbHeight = Utils.dp2px(56, context)
    private val trackInset = Utils.dp2px(8, context)
    private val hitWidth = Utils.dp2px(32, context)
    private val hitSlop = Utils.dp2px(8, context)

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        textAlign = Paint.Align.CENTER
    }
    private val rect = RectF()
    private val arrow = Path()
    private val exclusion = Rect()
    private var idleThumbAlpha = 0
    private var primaryColor = 0
    private var outlineColor = 0

    private val fadeOut = Runnable { if (!isDragging) hide() }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        alpha = 0f
        refreshTheme()
    }

    fun bind(view: NestedScrollView) {
        scrollView = view
    }

    fun refreshTheme() {
        primaryColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
        outlineColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutline)
        trackPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant)
        bubblePaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceInverse)
        textPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse)
        idleThumbAlpha = (0.85f * 255).toInt()
        invalidate()
    }

    /** 宿主的滚动监听里调用：显示滑块，并在停止滚动 1.5 秒后淡出。 */
    fun onContentScrolled() {
        if (scrollRange() <= 0) {
            hide()
            return
        }
        invalidate()
        if (isDragging) return
        reveal()
        removeCallbacks(fadeOut)
        postDelayed(fadeOut, HIDE_DELAY_MS)
    }

    /** 切换文章时立即收起。 */
    fun reset() {
        isDragging = false
        removeCallbacks(fadeOut)
        animate().cancel()
        alpha = 0f
        shown = false
        ViewCompat.setSystemGestureExclusionRects(this, emptyList())
    }

    private fun reveal() {
        if (shown && alpha == 1f) return
        shown = true
        animate().cancel()
        animate().alpha(1f).setDuration(FADE_IN_MS).start()
    }

    private fun hide() {
        if (!shown) return
        shown = false
        animate().cancel()
        animate().alpha(0f).setDuration(FADE_OUT_MS).withEndAction {
            if (!shown) ViewCompat.setSystemGestureExclusionRects(this, emptyList())
        }.start()
    }

    private fun contentHeight(): Int {
        val sv = scrollView ?: return 0
        val child = sv.getChildAt(0) ?: return 0
        return child.height + sv.paddingTop + sv.paddingBottom
    }

    private fun scrollRange(): Int {
        val sv = scrollView ?: return 0
        return ReadingScroll.range(contentHeight(), sv.height)
    }

    private fun trackHeight() = height - 2 * trackInset

    private fun progress() = ReadingScroll.progress(scrollView?.scrollY ?: 0, scrollRange())

    private fun thumbTop() = trackInset + FastScrollGeometry.thumbTop(progress(), trackHeight(), thumbHeight)

    override fun onDraw(canvas: Canvas) {
        if (!shown && alpha == 0f) return
        val dp = resources.displayMetrics.density
        val w = width.toFloat()
        val top = thumbTop()
        if (isDragging) {
            rect.set(w - 13 * dp, trackInset.toFloat(), w - 11 * dp, (height - trackInset).toFloat())
            canvas.drawRoundRect(rect, dp, dp, trackPaint.apply { alpha = 204 })
            thumbPaint.color = primaryColor
            thumbPaint.alpha = 255
            rect.set(w - 14 * dp, top, w - 6 * dp, top + thumbHeight)
        } else {
            thumbPaint.color = outlineColor
            thumbPaint.alpha = idleThumbAlpha
            rect.set(w - 8 * dp, top, w - 4 * dp, top + thumbHeight)
        }
        canvas.drawRoundRect(rect, 4 * dp, 4 * dp, thumbPaint)
        if (isDragging) drawBubble(canvas, top + thumbHeight / 2f, dp)
        if (shown) {
            exclusion.set((w - hitWidth).toInt(), (top - hitSlop).toInt(), width, (top + thumbHeight + hitSlop).toInt())
            ViewCompat.setSystemGestureExclusionRects(this, listOf(exclusion))
        }
    }

    private fun drawBubble(canvas: Canvas, centerY: Float, dp: Float) {
        val label = "${ReadingScroll.percent(progress())}%"
        val bubbleWidth = maxOf(64 * dp, textPaint.measureText(label) + 24 * dp)
        val bubbleHeight = 36 * dp
        val right = width - 30 * dp
        rect.set(right - bubbleWidth, centerY - bubbleHeight / 2, right, centerY + bubbleHeight / 2)
        canvas.drawRoundRect(rect, bubbleHeight / 2, bubbleHeight / 2, bubblePaint)
        arrow.reset()
        arrow.moveTo(right, centerY - 5 * dp)
        arrow.lineTo(right + 5 * dp, centerY)
        arrow.lineTo(right, centerY + 5 * dp)
        arrow.close()
        canvas.drawPath(arrow, bubblePaint)
        val baseline = centerY - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(label, rect.centerX(), baseline, textPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val sv = scrollView ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!shown || scrollRange() <= 0) return false
                val top = thumbTop()
                val inZone = event.x >= width - hitWidth &&
                    event.y >= top - hitSlop && event.y <= top + thumbHeight + hitSlop
                if (!inZone) return false
                grabOffset = (event.y - top).coerceIn(0f, thumbHeight.toFloat())
                isDragging = true
                removeCallbacks(fadeOut)
                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) return false
                val p = FastScrollGeometry.progressAt(
                    event.y - trackInset, grabOffset, trackHeight(), thumbHeight
                )
                sv.scrollTo(0, ReadingScroll.scrollOffset(p, scrollRange()))
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isDragging) return false
                isDragging = false
                invalidate()
                postDelayed(fadeOut, HIDE_DELAY_MS)
                return true
            }
        }
        return isDragging
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(fadeOut)
        animate().cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val HIDE_DELAY_MS = 1500L
        const val FADE_IN_MS = 120L
        const val FADE_OUT_MS = 250L
    }
}
