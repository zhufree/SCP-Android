package info.free.scp.view.feed

import android.content.Intent
import android.os.Bundle
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.databinding.ActivityTopRatedBinding
import info.free.scp.view.base.BaseActivity

class TopRatedActivity : BaseActivity() {
    private lateinit var binding: ActivityTopRatedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopRatedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.topRatedToolbar
        binding.cvTopRatedAll.setOnClickListener {
            openTopRatedList(SCPConstants.TOP_RATED_ALL)
        }
        binding.cvTopRatedScp.setOnClickListener {
            openTopRatedList(SCPConstants.TOP_RATED_SCP)
        }
        binding.cvTopRatedTales.setOnClickListener {
            openTopRatedList(SCPConstants.TOP_RATED_TALES)
        }
        binding.cvTopRatedGoi.setOnClickListener {
            openTopRatedList(SCPConstants.TOP_RATED_GOI)
        }
        binding.cvTopRatedWanderer.setOnClickListener {
            openTopRatedList(SCPConstants.TOP_RATED_WANDERS)
        }
    }

    private fun openTopRatedList(feedType: Int) {
        startActivity(
            Intent(this, TopRatedListActivity::class.java)
                .putExtra("feedType", feedType)
        )
    }
}
