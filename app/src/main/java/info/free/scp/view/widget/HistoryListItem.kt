package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import kotlinx.android.synthetic.main.layout_history_item.view.*

class HistoryListItem : ConstraintLayout {
    var title = ""
    var time = ""
    var onItemClick: () -> Unit = {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        attrs?.let { retrieveAttributes(attrs) }
    }

    constructor(context: Context, title: String, time: String) : this(context, null) {
        initView(context)
        tv_history_item_title.text = title
        tv_history_item_time.text = time
        setOnClickListener { onItemClick() }
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.layout_history_item, this)
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.HistoryListItem)

        // 赋值给属性变量
        title = ta.getString(R.styleable.HistoryListItem_historyTitle) ?: ""
        time = ta.getString(R.styleable.HistoryListItem_time) ?: ""
        ta.recycle()

        tv_history_item_title.text = title
        tv_history_item_time.text = time
        setOnClickListener { onItemClick() }
    }

    fun refreshTheme() {
        tv_history_item_title.setTextColor(ThemeUtil.darkText)
        tv_history_item_time.setTextColor(ThemeUtil.mediumText)
    }
}