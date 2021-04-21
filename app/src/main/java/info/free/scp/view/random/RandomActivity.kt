package info.free.scp.view.random

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import info.free.scp.R
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.category.GroupViewModel
import kotlinx.android.synthetic.main.activity_random.*
import org.jetbrains.anko.dip

class RandomActivity : BaseActivity() {
    val adapter: RandomAdapter by lazy {
        RandomAdapter()
    }
    private val vm: GroupViewModel by lazy {
        ViewModelProvider(this)
                .get(GroupViewModel::class.java)
    }
    private var randomType = 0 // 0 所有，1仅scp，2 故事，3 joke
    private val randomRange: String
        get() = when (randomType) {
            1 -> "1,2"
            2 -> "7,8"
            3 -> "3,4"
            else -> ""
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random)
        randomType = intent.getIntExtra("random_type", 0)
        baseToolbar = random_toolbar
        supportActionBar?.title = intent.getStringExtra("random_title") ?: "随机"
        rv_random_list.adapter = adapter
        rv_random_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_SETTLING) {
                    btn_refresh_random.visibility = VISIBLE
                }
                if (newState == SCROLL_STATE_DRAGGING) {
                    btn_refresh_random.visibility = GONE
                }
            }
        })

        btn_refresh_random.background = ThemeUtil.customShape(Color.parseColor("#4A90E2"),
                0, 0, dip(32))
        btn_refresh_random.setOnClickListener {
            vm.refreshRandomList(randomRange)
        }
        vm.getRandomList().observe(this, Observer { list ->
            adapter.submitList(list)
        })
        vm.refreshRandomList(randomRange)
    }
}