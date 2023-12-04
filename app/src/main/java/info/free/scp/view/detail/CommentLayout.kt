package info.free.scp.view.detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.bean.CommentModel
import info.free.scp.databinding.LayoutCommentBinding
import info.free.scp.util.ThemeUtil
import org.jetbrains.anko.collections.forEachWithIndex

class CommentLayout : ConstraintLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private lateinit var binding: LayoutCommentBinding
    private fun initView(context: Context) {
        binding = LayoutCommentBinding.inflate(
            LayoutInflater.from(context), this
        )
    }

    val replyList = emptyList<CommentLayout>().toMutableList()

    fun setData(comment: CommentModel) {
        if (comment.title.isEmpty()) {
            binding.tvCommentTitle.visibility = GONE
        } else {
            binding.tvCommentTitle.text = comment.title
        }
        binding.tvCommentUsername.text = comment.username
        binding.tvCommentTime.text = comment.time
        binding.tvCommentContent.text = comment.comment
        val replyLp = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        binding.tvCommentContent.post {
            comment.reply.forEachWithIndex { index, it ->
                val replyLayout = CommentLayout(this.context)
                replyLayout.setData(it)
                binding.llReplyContainer.addView(replyLayout, replyLp)
                replyList.add(replyLayout)
            }
        }
    }

    fun refreshTheme() {
        binding.tvCommentTitle.setTextColor(ThemeUtil.darkText)
        binding.tvCommentContent.setTextColor(ThemeUtil.darkText)
        binding.tvCommentUsername.setTextColor(ThemeUtil.mediumText)
        binding.tvCommentTime.setTextColor(ThemeUtil.mediumText)
        replyList.forEach { it.refreshTheme() }
    }
}