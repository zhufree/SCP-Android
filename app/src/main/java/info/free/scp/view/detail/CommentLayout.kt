package info.free.scp.view.detail

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.bean.CommentModel
import info.free.scp.util.ThemeUtil
import kotlinx.android.synthetic.main.layout_comment.view.*
import org.jetbrains.anko.collections.forEachWithIndex

class CommentLayout : ConstraintLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.layout_comment, this)
    }

    val replyList = emptyList<CommentLayout>().toMutableList()

    fun setData(comment: CommentModel) {
        if (comment.title.isEmpty()) {
            tv_comment_title.visibility = GONE
        } else {
            tv_comment_title.text = comment.title
        }
        tv_comment_username.text = comment.username
        tv_comment_time.text = comment.time
        tv_comment_content.text = comment.comment
        val replyLp = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        tv_comment_content.post {
            comment.reply.forEachWithIndex { index, it ->
                val replyLayout = CommentLayout(this.context)
                replyLayout.setData(it)
                ll_reply_container.addView(replyLayout, replyLp)
                replyList.add(replyLayout)
            }
        }
    }

    fun refreshTheme() {
        tv_comment_title.setTextColor(ThemeUtil.darkText)
        tv_comment_content.setTextColor(ThemeUtil.darkText)
        tv_comment_username.setTextColor(ThemeUtil.mediumText)
        tv_comment_time.setTextColor(ThemeUtil.mediumText)
        replyList.forEach { it.refreshTheme() }
    }
}