package info.free.scp.view.detail

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import info.free.scp.R
import info.free.scp.bean.CommentModel
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.layout_comment.view.*

class CommentLayout : ConstraintLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.layout_comment, this)
    }

    fun setData(comment: CommentModel) {
        if (comment.title.isEmpty()) {
            tv_comment_title.visibility = GONE
        } else {
            tv_comment_title.text = comment.title
        }
        tv_comment_username.text = comment.username
        tv_comment_time.text = comment.time
        tv_comment_content.text = comment.comment
        val replyLp = LayoutParams(0, WRAP_CONTENT)
        replyLp.topToBottom = tv_comment_content.id
        replyLp.startToStart = tv_comment_content.id
        replyLp.endToEnd = PARENT_ID
        replyLp.marginStart = Utils.dp2px(16)
        replyLp.topMargin = Utils.dp2px(8)
        tv_comment_content.post {
            comment.reply.forEach {
                val replyLayout = CommentLayout(this.context)
                replyLayout.setData(it)
                addView(replyLayout, replyLp)
                replyLp.topToBottom = replyLayout.id
            }
        }
    }
}