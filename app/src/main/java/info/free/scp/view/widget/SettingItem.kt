package info.free.scp.view.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.databinding.LayoutSettingItemBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import org.jetbrains.anko.backgroundColor


class SettingItem : ConstraintLayout {
    private lateinit var binding: LayoutSettingItemBinding
    var title = ""
    var rightVisible = false
    var rightText = ""
    var isGroupFirst = false
    var isGroupLast = false
    var onClick: () -> Unit = {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
        attrs?.let { retrieveAttributes(attrs) }
    }


    private fun initView(context: Context) {
        binding = LayoutSettingItemBinding.inflate(LayoutInflater.from(context), this)
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        // 从attrs定义文件中获取属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItem)

        // 赋值给属性变量
        rightVisible = ta.getBoolean(R.styleable.SettingItem_settingRightVisible, false)
        title = ta.getString(R.styleable.SettingItem_name) ?: ""
        rightText = ta.getString(R.styleable.SettingItem_rightText) ?: ""
        isGroupFirst = ta.getBoolean(R.styleable.SettingItem_isGroupFirst, false)
        isGroupLast = ta.getBoolean(R.styleable.SettingItem_isGroupLast, false)

        ta.recycle()

        binding.tvSettingTitle.text = title
        binding.tvSettingRight.visibility = if (rightVisible) View.VISIBLE else View.GONE
        if (rightVisible) {
            binding.tvSettingRight.text = rightText
        }

        refreshTheme()
        setOnClickListener {
            onClick()
        }
    }

    fun refreshTheme() {
        binding.tvSettingTitle.setTextColor(ThemeUtil.darkText)
        val cornerRadius = Utils.dp2px(18).toFloat()
        when {
            isGroupFirst -> {
                background = ThemeUtil.customShape(ThemeUtil.itemBg, 0, 0, cornerRadius, 0f, cornerRadius, 0f)
            }
            isGroupLast -> {
                background = ThemeUtil.customShape(ThemeUtil.itemBg, 0, 0, 0f, cornerRadius, 0f, cornerRadius)
            }
            else -> {
                backgroundColor = ThemeUtil.itemBg
            }
        }
    }

    fun changeTitle(title: String) {
        binding.tvSettingTitle.text = title
    }

    fun setRight(msg: String) {
        binding.tvSettingRight.text = msg
        binding.tvSettingRight.setTextColor(Color.RED)
    }
}