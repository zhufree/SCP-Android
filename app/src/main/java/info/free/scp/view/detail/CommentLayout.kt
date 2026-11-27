package info.free.scp.view.detail

import android.content.Context
import android.util.AttributeSet
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.color.MaterialColors
import info.free.scp.bean.CommentModel
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils

class CommentLayout : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private val accentWidth = Utils.dp2px(2, context)
    private val smallGap = Utils.dp2px(4, context)
    private val replyIndent = Utils.dp2px(14, context)
    private val dividerView = android.view.View(context)
    private val titleView = TextView(context)
    private val metaRow = LinearLayout(context)
    private val usernameView = TextView(context)
    private val timeView = TextView(context)
    private val contentRow = LinearLayout(context)
    private val accentView = android.view.View(context)
    private val contentColumn = LinearLayout(context)
    private val contentView = TextView(context)
    private val replyContainer = LinearLayout(context)

    val replyList = mutableListOf<CommentLayout>()

    private fun initView() {
        orientation = VERTICAL
        addView(dividerView, LayoutParams(MATCH_PARENT, Utils.dp2px(1, context)))

        titleView.apply {
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        addView(titleView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = Utils.dp2px(10, context) })

        metaRow.orientation = HORIZONTAL
        usernameView.textSize = 12f
        timeView.textSize = 12f
        metaRow.addView(usernameView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        metaRow.addView(
            timeView,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { marginStart = Utils.dp2px(16, context) }
        )
        addView(
            metaRow,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = smallGap }
        )

        contentRow.orientation = HORIZONTAL
        accentView.setBackgroundColor(ThemeUtil.accentColor)
        contentRow.addView(
            accentView,
            LayoutParams(accentWidth, MATCH_PARENT).apply { marginEnd = Utils.dp2px(2, context) }
        )

        contentColumn.orientation = VERTICAL
        contentView.textSize = 15f
        contentView.setLineSpacing(0f, 1.5f)
        contentColumn.addView(contentView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        replyContainer.orientation = VERTICAL
        contentColumn.addView(
            replyContainer,
            LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                marginStart = replyIndent
                topMargin = smallGap
            }
        )
        contentRow.addView(contentColumn, LayoutParams(0, WRAP_CONTENT, 1f))
        addView(
            contentRow,
            LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = smallGap
                bottomMargin = Utils.dp2px(10, context)
            }
        )

        refreshTheme()
    }

    /** 顶层评论用分隔线隔开；回复缩进，左侧带一条强调线。 */
    fun setData(comment: CommentModel, isReply: Boolean = false) {
        dividerView.visibility = if (isReply) GONE else VISIBLE
        accentView.visibility = if (isReply) VISIBLE else GONE
        titleView.visibility = if (comment.title.isEmpty()) GONE else VISIBLE
        titleView.text = comment.title
        usernameView.text = comment.username
        timeView.text = comment.time
        contentView.text = comment.comment

        replyContainer.removeAllViews()
        replyList.clear()
        val replyLp = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        contentView.post {
            comment.reply.forEach { reply ->
                val replyLayout = CommentLayout(context)
                replyLayout.setData(reply, isReply = true)
                replyContainer.addView(replyLayout, replyLp)
                replyList.add(replyLayout)
            }
        }
    }

    fun refreshTheme() {
        dividerView.setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant))
        titleView.setTextColor(ThemeUtil.darkText)
        contentView.setTextColor(ThemeUtil.darkText)
        usernameView.setTextColor(ThemeUtil.mediumText)
        timeView.setTextColor(ThemeUtil.mediumText)
        replyList.forEach { it.refreshTheme() }
    }
}
