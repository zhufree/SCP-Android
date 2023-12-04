package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.databinding.LayoutEntryItemBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import org.jetbrains.anko.dip
import org.jetbrains.anko.padding

class EntryItem : ConstraintLayout {
    var title = ""
    var logo = 0
    var bgColor = 0
    var imgPadding = 0f
    var onClick: () -> Unit = {}

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
        attrs?.let { retrieveAttributes(attrs) }
    }

    private lateinit var binding: LayoutEntryItemBinding

    private fun initView(context: Context) {
        binding = LayoutEntryItemBinding.inflate(LayoutInflater.from(context), this)
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.EntryItem)

        val screenWidth = Utils.getScreenWidth(context)
        // 赋值给属性变量
        title = ta.getString(R.styleable.EntryItem_entryTitle) ?: ""
        logo = ta.getResourceId(R.styleable.EntryItem_imgSrc, R.drawable.img_logo_cn)
        bgColor = ta.getColor(R.styleable.EntryItem_imgBg, ThemeUtil.itemBg)
        imgPadding = ta.getDimension(R.styleable.EntryItem_imgPadding, 5f)
        val count = ta.getInteger(R.styleable.EntryItem_count, 3)
        ta.recycle()
        maxWidth = (screenWidth - dip(16)) / count

        binding.tvEntryTitle.text = title
        binding.ivEntryLogo.padding = imgPadding.toInt()
        binding.ivEntryLogo.background = ThemeUtil.customShape(bgColor, 0, 0, dip(25))
        binding.ivEntryLogo.setImageResource(logo)
        setOnClickListener {
            onClick()
        }
    }

    fun refreshTheme() {
        binding.tvEntryTitle.setTextColor(ThemeUtil.darkText)
        binding.ivEntryLogo.background = ThemeUtil.customShape(bgColor, 0, 0, dip(25))
    }
}