package info.free.scp.view.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import kotlinx.android.synthetic.main.layout_top_bar.view.*


class TopBar : ConstraintLayout {

    var title = ""
    var titleTextSize = 16f
    var titleTextColor = Color.BLACK
    var leftVisible = true
    var rightVisible = false
    var rightIcon = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        attrs?.let { retrieveAttributes(attrs) }
    }


    private fun initView(context: Context) {
        inflate(context, R.layout.layout_top_bar, this)

    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        // 从attrs定义文件中获取属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TopBar)

        // 赋值给属性变量
        leftVisible = ta.getBoolean(R.styleable.TopBar_leftVisible, true)
        rightVisible = ta.getBoolean(R.styleable.TopBar_rightVisible, false)
        title = ta.getString(R.styleable.TopBar_topbarTitle) ?: ""
        titleTextSize = ta.getDimension(R.styleable.TopBar_titleTextSize, 16f)
        titleTextColor = ta.getColor(R.styleable.TopBar_titleTextColor, context.resources.getColor(R.color.colorPrimaryDark))
        rightIcon = ta.getResourceId(R.styleable.TopBar_rightIcon, R.drawable.ic_autorenew_white_24dp)

        ta.recycle()

        tv_top_title.text = title
        iv_left_icon.visibility = if (leftVisible) View.VISIBLE else View.GONE
        iv_right_icon.visibility = if (rightVisible) View.VISIBLE else View.GONE
        tv_top_title.textSize = titleTextSize
        tv_top_title.setTextColor(titleTextColor)
        iv_right_icon.setImageResource(rightIcon)
    }
}