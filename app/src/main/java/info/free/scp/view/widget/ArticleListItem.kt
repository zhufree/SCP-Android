package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.layout_article_item.view.*
import kotlinx.android.synthetic.main.layout_entry_item.view.*
import org.jetbrains.anko.dip

class ArticleListItem : ConstraintLayout {
    var title = ""
    var rank = ""
    var onLaterClick: () -> Unit = {}
    var onItemClick: () -> Unit = {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        attrs?.let { retrieveAttributes(attrs) }
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.layout_article_item, this)
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ArticleListItem)

        // 赋值给属性变量
        title = ta.getString(R.styleable.ArticleListItem_articleTitle) ?: ""
        rank = ta.getString(R.styleable.ArticleListItem_rank) ?: ""
        ta.recycle()

        tv_article_item_title.text = title
        tv_article_item_rank.text = rank
        cl_read_later.setOnClickListener {
            onLaterClick()
        }
        setOnClickListener { onItemClick() }
    }

    fun refreshTheme() {
        tv_entry_title.setTextColor(ThemeUtil.darkText)
    }
}