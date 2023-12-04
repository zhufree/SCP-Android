package info.free.scp.view.random

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import info.free.scp.R
import info.free.scp.databinding.ActivityRandomBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.category.GroupViewModel
import org.jetbrains.anko.dip

class RandomActivity : BaseActivity() {
    private lateinit var binding: ActivityRandomBinding
    val adapter: RandomAdapter by lazy {
        RandomAdapter()
    }
    private val vm: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
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
        binding = ActivityRandomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        randomType = intent.getIntExtra("random_type", 0)
        baseToolbar = binding.randomToolbar
        supportActionBar?.title = intent.getStringExtra("random_title") ?: "随机"
        binding.rvRandomList.adapter = adapter
        binding.rvRandomList.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_SETTLING) {
                    binding.btnRefreshRandom.visibility = VISIBLE
                    binding.btnAddMoreRandom.visibility = VISIBLE
                }
                if (newState == SCROLL_STATE_DRAGGING) {
                    binding.btnRefreshRandom.visibility = GONE
                    binding.btnAddMoreRandom.visibility = GONE
                }
            }
        })

        binding.btnRefreshRandom.background = ThemeUtil.customShape(
            Color.parseColor("#4A90E2"),
            0, 0, dip(32)
        )
        binding.btnAddMoreRandom.background = ThemeUtil.customShape(
            Color.parseColor("#4A90E2"),
            0, 0, dip(32)
        )
        binding.btnRefreshRandom.setOnClickListener {
            vm.refreshRandomList(randomRange)
        }
        binding.btnAddMoreRandom.setOnClickListener {
            vm.refreshRandomList(randomRange, true)
        }
        vm.getRandomList().observe(this) { list ->
            adapter.submitList(list)
        }
        vm.refreshRandomList(randomRange)
    }
}