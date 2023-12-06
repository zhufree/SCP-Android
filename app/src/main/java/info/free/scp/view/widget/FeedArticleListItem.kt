package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.bean.FeedModel
import info.free.scp.databinding.LayoutFeedArticleItemBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.startActivity

class FeedArticleListItem : ConstraintLayout {
    private lateinit var binding: LayoutFeedArticleItemBinding
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    constructor(context: Context, feed: FeedModel) : this(context, null) {
        initView(context)
        binding.tvFeedItemTitle.text = feed.title
        binding.tvFeedItemTime.text = feed.createdTime
        binding.tvFeedItemRank.text = feed.rank
        setOnClickListener {
            context.startActivity<DetailActivity>(
                "link" to feed.link,
                "forceOnline" to true
            )
        }
    }

    private fun initView(context: Context) {
        binding = LayoutFeedArticleItemBinding.inflate(LayoutInflater.from(context), this)
    }

    fun refreshTheme() {
        backgroundColor = ThemeUtil.itemBg
        binding.tvFeedItemTitle.setTextColor(ThemeUtil.darkText)
        binding.tvFeedItemTime.setTextColor(ThemeUtil.mediumText)
        binding.tvFeedItemRank.setTextColor(ThemeUtil.mediumText)
        binding.vFeedDivider.setBackgroundColor(ThemeUtil.dividerGray)
    }
}