package info.free.scp.view.feed

import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.databinding.ActivityTopRatedBinding
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.startActivity

class TopRatedActivity : BaseActivity() {
    private lateinit var binding: ActivityTopRatedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopRatedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.topRatedToolbar
        binding.cvTopRatedAll.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_ALL)
        }
        binding.cvTopRatedScp.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_SCP)
        }
        binding.cvTopRatedTales.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_TALES)
        }
        binding.cvTopRatedGoi.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_GOI)
        }
        binding.cvTopRatedWanderer.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to SCPConstants.TOP_RATED_WANDERS)
        }
    }
}