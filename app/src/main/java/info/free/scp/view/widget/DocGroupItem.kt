package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.databinding.LayoutGroupItemBinding
import info.free.scp.util.ThemeUtil

class DocGroupItem : ConstraintLayout {
    var title = ""

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private lateinit var binding: LayoutGroupItemBinding
    constructor(context: Context, title: String) : this(context, null) {
        binding.tvGroupItemTitle.text = title
    }

    private fun initView(context: Context) {
        binding = LayoutGroupItemBinding.inflate(LayoutInflater.from(context))
        background = ThemeUtil.getDrawable(context, R.drawable.bg_entry_box)
    }

    fun refreshTheme() {
        // TODO
    }
}