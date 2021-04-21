package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.bean.FeedModel
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.layout_feed_article_item.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.startActivity

class FeedArticleListItem : ConstraintLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    constructor(context: Context, feed: FeedModel) : this(context, null) {
        initView(context)
        tv_feed_item_title.text = feed.title
        tv_feed_item_time.text = feed.createdTime
        tv_feed_item_rank.text = feed.rank
        setOnClickListener {
            context.startActivity<DetailActivity>(
                    "link" to feed.link,
                    "forceOnline" to true
            )
        }
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.layout_feed_article_item, this)
    }

    fun refreshTheme() {
        backgroundColor = ThemeUtil.itemBg
        tv_feed_item_title.setTextColor(ThemeUtil.darkText)
        tv_feed_item_time.setTextColor(ThemeUtil.mediumText)
        tv_feed_item_rank.setTextColor(ThemeUtil.mediumText)
        v_feed_divider.setBackgroundColor(ThemeUtil.dividerGray)
    }
}