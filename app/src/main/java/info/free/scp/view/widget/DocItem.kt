package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import kotlinx.android.synthetic.main.layout_group_item.view.*

class DocItem : ConstraintLayout {
    var title = ""

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    constructor(context: Context, title: String) : this(context, null) {
        tv_group_item_title.text = title
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.layout_group_item, this)
        background = ThemeUtil.getDrawable(context, R.drawable.bg_entry_box)
    }

    fun refreshTheme() {
        // TODO
    }
}