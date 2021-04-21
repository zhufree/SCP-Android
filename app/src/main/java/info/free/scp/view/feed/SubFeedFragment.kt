package info.free.scp.view.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import info.free.scp.SCPConstants.LATEST_CREATED

import info.free.scp.databinding.SubFeedFragmentBinding
import info.free.scp.view.base.BaseFragment

class SubFeedFragment : BaseFragment() {

    var feedType = LATEST_CREATED

    companion object {
        fun newInstance(feedType: Int): SubFeedFragment {
            val fragment = SubFeedFragment()
            val args = Bundle()
            args.putInt("feedType", feedType)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(this)
                .get(FeedListViewModel::class.java)
    }

    private val feedAdapter by lazy {
        FeedAdapter()
    }

    private lateinit var binding: SubFeedFragmentBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = SubFeedFragmentBinding.inflate(inflater, container, false)
        binding.rvFeed.adapter = feedAdapter
        binding.slFeed.isRefreshing = true
        subscribeUi(feedAdapter)
        binding.slFeed.setOnRefreshListener {
            binding.slFeed.isRefreshing = false
        }
        return binding.root
    }


    private fun subscribeUi(adapter: FeedAdapter) {
        feedType = arguments?.getInt("feedType") ?: LATEST_CREATED
        Log.i("feed", "type = $feedType")
        viewModel.getFeed().observe(viewLifecycleOwner, Observer { result ->
            binding.slFeed.isRefreshing = false
            if (result != null && result.isNotEmpty())
                adapter.submitList(result)
        })
        viewModel.loadFeed(feedType)
    }

    override fun refreshTheme() {
        super.refreshTheme()
        feedAdapter.refreshTheme()
    }
}
