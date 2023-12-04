package info.free.scp.view.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.databinding.LayoutTopBarBinding


class TopBar : ConstraintLayout {
    private lateinit var binding: LayoutTopBarBinding
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
        binding = LayoutTopBarBinding.inflate(LayoutInflater.from(context))
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        // 从attrs定义文件中获取属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TopBar)

        // 赋值给属性变量
        leftVisible = ta.getBoolean(R.styleable.TopBar_leftVisible, true)
        rightVisible = ta.getBoolean(R.styleable.TopBar_rightVisible, false)
        title = ta.getString(R.styleable.TopBar_topbarTitle) ?: ""
        titleTextSize = ta.getDimension(R.styleable.TopBar_titleTextSize, 16f)
        titleTextColor = ta.getColor(
            R.styleable.TopBar_titleTextColor,
            context.resources.getColor(R.color.colorPrimaryDark)
        )
        rightIcon =
            ta.getResourceId(R.styleable.TopBar_rightIcon, R.drawable.baseline_add_white_24dp)

        ta.recycle()

        binding.tvTopTitle.text = title
        binding.ivLeftIcon.visibility = if (leftVisible) View.VISIBLE else View.GONE
        binding.ivRightIcon.visibility = if (rightVisible) View.VISIBLE else View.GONE
        binding.tvTopTitle.textSize = titleTextSize
        binding.tvTopTitle.setTextColor(titleTextColor)
        binding.ivRightIcon.setImageResource(rightIcon)
    }
}