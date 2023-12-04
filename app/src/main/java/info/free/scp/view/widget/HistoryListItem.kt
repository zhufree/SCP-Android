package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.databinding.LayoutHistoryItemBinding
import info.free.scp.util.ThemeUtil

class HistoryListItem : ConstraintLayout {
    var title = ""
    var time = ""
    var onItemClick: () -> Unit = {}
    private lateinit var binding: LayoutHistoryItemBinding
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        attrs?.let { retrieveAttributes(attrs) }
    }

    constructor(context: Context, title: String, time: String) : this(context, null) {
        initView(context)
        binding.tvHistoryItemTitle.text = title
        binding.tvHistoryItemTime.text = time
        setOnClickListener { onItemClick() }
    }

    private fun initView(context: Context) {
        binding = LayoutHistoryItemBinding.inflate(LayoutInflater.from(context), this)
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.HistoryListItem)

        // 赋值给属性变量
        title = ta.getString(R.styleable.HistoryListItem_historyTitle) ?: ""
        time = ta.getString(R.styleable.HistoryListItem_time) ?: ""
        ta.recycle()

        binding.tvHistoryItemTitle.text = title
        binding.tvHistoryItemTime.text = time
        setOnClickListener { onItemClick() }
    }

    fun refreshTheme() {
        binding.tvHistoryItemTitle.setTextColor(ThemeUtil.darkText)
        binding.tvHistoryItemTime.setTextColor(ThemeUtil.mediumText)
    }
}