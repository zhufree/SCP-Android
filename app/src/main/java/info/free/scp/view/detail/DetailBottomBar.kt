package info.free.scp.view.detail

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.color.MaterialColors
import info.free.scp.R
import info.free.scp.util.Utils

/**
 * 阅读页底栏：顶部 2dp 阅读进度线 + 56dp 的“上一篇 / 评论 / 标记已读 / 下一篇”。
 * 每一项的触控区都是整格（高 56dp），收起时整体下移 [barHeight]，进度线留在原位。
 */
class DetailBottomBar(context: Context) : LinearLayout(context) {
    val barHeight = Utils.dp2px(56, context)
    private val lineHeight = Utils.dp2px(2, context)

    val previous: View
    val comment: View
    val read: View
    val next: View

    private val progressLine = ProgressLine(context)
    private val row = FrameLayout(context)
    private val divider = View(context)
    private val iconViews = mutableListOf<ImageView>()
    private val navTexts = mutableListOf<TextView>()
    private val readPill = LinearLayout(context)
    private val readIcon = ImageView(context)
    private val readLabel = TextView(context)
    private var hasRead = false

    init {
        orientation = VERTICAL
        addView(progressLine, LayoutParams(MATCH_PARENT, lineHeight))
        addView(row, LayoutParams(MATCH_PARENT, barHeight))

        val cells = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(Utils.dp2px(8, context), 0, Utils.dp2px(8, context), 0)
        }
        row.addView(cells, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        row.addView(divider, FrameLayout.LayoutParams(MATCH_PARENT, Utils.dp2px(1, context)))

        previous = cell(R.id.tv_bottom_preview).also {
            it.addView(
                horizontal(icon(R.drawable.ic_reading_prev, 20), label(R.string.preview_article, 14f)),
                FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            )
            cells.addView(it, LayoutParams(0, MATCH_PARENT, 1f))
        }
        comment = cell(R.id.btn_comment).also {
            val column = LinearLayout(context).apply {
                orientation = VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                addView(icon(R.drawable.ic_reading_comment, 22))
                addView(label(R.string.reading_comments, 11f))
            }
            it.addView(column, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER))
            cells.addView(it, LayoutParams(0, MATCH_PARENT, 0.65f))
        }
        read = cell(R.id.tv_bottom_set_has_read).also {
            readIcon.apply {
                setImageResource(R.drawable.ic_reading_check)
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            readLabel.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                setSingleLine(true)
            }
            readPill.apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER
                addView(readIcon, LayoutParams(Utils.dp2px(20, context), Utils.dp2px(20, context)).apply {
                    marginEnd = Utils.dp2px(6, context)
                })
                addView(readLabel, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
            }
            it.addView(
                readPill,
                FrameLayout.LayoutParams(MATCH_PARENT, Utils.dp2px(40, context), Gravity.CENTER_VERTICAL).apply {
                    marginStart = Utils.dp2px(4, context)
                    marginEnd = Utils.dp2px(4, context)
                }
            )
            cells.addView(it, LayoutParams(0, MATCH_PARENT, 1.15f))
        }
        next = cell(R.id.tv_bottom_next).also {
            it.addView(
                horizontal(label(R.string.next_article, 14f), icon(R.drawable.ic_reading_next, 20)),
                FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            )
            cells.addView(it, LayoutParams(0, MATCH_PARENT, 1f))
        }
        setRead(false)
        refreshTheme()
    }

    fun setProgress(progress: Float) {
        progressLine.progress = progress
    }

    fun setRead(hasRead: Boolean) {
        this.hasRead = hasRead
        readLabel.setText(if (hasRead) R.string.has_read else R.string.set_has_read)
        readIcon.visibility = if (hasRead) VISIBLE else GONE
        applyReadColors()
    }

    fun refreshTheme() {
        setBackgroundColor(color(com.google.android.material.R.attr.colorSurfaceContainer))
        row.setBackgroundColor(color(com.google.android.material.R.attr.colorSurfaceContainer))
        divider.setBackgroundColor(color(com.google.android.material.R.attr.colorOutlineVariant))
        val onSurfaceVariant = color(com.google.android.material.R.attr.colorOnSurfaceVariant)
        iconViews.forEach { it.imageTintList = ColorStateList.valueOf(onSurfaceVariant) }
        navTexts.forEach { it.setTextColor(onSurfaceVariant) }
        progressLine.refreshTheme()
        applyReadColors()
    }

    private fun applyReadColors() {
        val fill: Int
        val onFill: Int
        if (hasRead) {
            fill = color(com.google.android.material.R.attr.colorSecondaryContainer)
            onFill = color(com.google.android.material.R.attr.colorOnSecondaryContainer)
        } else {
            fill = color(androidx.appcompat.R.attr.colorPrimary)
            onFill = color(com.google.android.material.R.attr.colorOnPrimary)
        }
        readPill.background = GradientDrawable().apply {
            setColor(fill)
            cornerRadius = Utils.dp2px(20, context).toFloat()
        }
        readLabel.setTextColor(onFill)
        readIcon.imageTintList = ColorStateList.valueOf(onFill)
    }

    private fun color(attr: Int) = MaterialColors.getColor(this, attr)

    private fun cell(viewId: Int) = FrameLayout(context).apply {
        id = viewId
        isClickable = true
        isFocusable = true
        val value = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
        foreground = context.getDrawable(value.resourceId)
    }

    private fun horizontal(vararg children: View) = LinearLayout(context).apply {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        children.forEach { addView(it) }
    }

    private fun icon(resId: Int, sizeDp: Int) = ImageView(context).apply {
        setImageResource(resId)
        layoutParams = LayoutParams(Utils.dp2px(sizeDp, context), Utils.dp2px(sizeDp, context))
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        iconViews += this
    }

    private fun label(textRes: Int, sizeSp: Float) = TextView(context).apply {
        setText(textRes)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setSingleLine(true)
        navTexts += this
    }

    /** 2dp 阅读进度线：轨道用 outlineVariant，已读部分用主色。 */
    private class ProgressLine(context: Context) : View(context) {
        var progress = 0f
            set(value) {
                val clamped = value.coerceIn(0f, 1f)
                if (field != clamped) {
                    field = clamped
                    invalidate()
                }
            }
        private val paint = Paint()
        private var trackColor = 0
        private var fillColor = 0

        init {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        fun refreshTheme() {
            trackColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant)
            fillColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            paint.color = trackColor
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = fillColor
            canvas.drawRect(0f, 0f, width * progress, height.toFloat(), paint)
        }
    }
}
