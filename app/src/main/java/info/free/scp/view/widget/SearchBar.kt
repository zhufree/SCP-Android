package info.free.scp.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED
import androidx.constraintlayout.widget.ConstraintLayout
import info.free.scp.R
import info.free.scp.databinding.LayoutSearchBarBinding
import info.free.scp.util.EventUtil
import info.free.scp.view.home.DirectActivity
import info.free.scp.view.random.RandomActivity
import info.free.scp.view.search.SearchActivity
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import toast

class SearchBar : ConstraintLayout {
    private lateinit var binding: LayoutSearchBarBinding
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        binding = LayoutSearchBarBinding.inflate(LayoutInflater.from(context))
        binding.btnDirect?.setOnClickListener {
            context.startActivity<DirectActivity>()
        }
        binding.btnRandom?.setOnClickListener {
            val randomTypes = listOf("随机全部", "随机SCP", "随机故事", "随机搞笑")
            context.selector("", randomTypes) { _, i ->
                when (i) {
                    0 -> {
                        EventUtil.onEvent(context, EventUtil.clickRandomAll)
                        context.startActivity<RandomActivity>(
                            "random_type" to 0,
                            "random_title" to randomTypes[i]
                        )
                    }

                    1 -> {
                        EventUtil.onEvent(context, EventUtil.clickRandomScp)
                        context.startActivity<RandomActivity>("random_type" to 1,
                                "random_title" to randomTypes[i])
                    }
                    2 -> {
                        EventUtil.onEvent(context, EventUtil.clickRandomTale)
                        context.startActivity<RandomActivity>("random_type" to 2,
                                "random_title" to randomTypes[i])
                    }
                    3 -> {
                        EventUtil.onEvent(context, EventUtil.clickRandomJoke)
                        context.startActivity<RandomActivity>("random_type" to 3,
                                "random_title" to randomTypes[i])
                    }
                }
            }
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if ((actionId == IME_ACTION_UNSPECIFIED || actionId == IME_ACTION_SEARCH)) {
                val keyword = binding.etSearch.text.toString().trim()
                if (keyword.isEmpty()) {
                    toast("请输入正确的关键词")
                } else {
                    context.startActivity<SearchActivity>("keyword" to keyword)
//                    if (searchMode == 0) searchByTitle(keyword) else searchByDetail(keyword)
                }
            }
            true
        }
    }
}