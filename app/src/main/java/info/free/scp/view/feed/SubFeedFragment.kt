package info.free.scp.view.feed

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import info.free.scp.SCPConstants.LATEST_CREATED

import info.free.scp.databinding.SubFeedFragmentBinding
import info.free.scp.util.InjectorUtils

class SubFeedFragment : Fragment() {

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

    private lateinit var viewModel: FeedListViewModel


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = SubFeedFragmentBinding.inflate(inflater, container, false)
        val feedAdapter = FeedAdapter()
        binding.rvFeed.adapter = feedAdapter
        subscribeUi(feedAdapter)
        binding.slFeed.setOnRefreshListener {
            binding.slFeed.isRefreshing = false
        }
        return binding.root
    }


    private fun subscribeUi(adapter: FeedAdapter) {
        val factory = InjectorUtils.provideFeedListViewModelFactory(requireContext())
        viewModel = ViewModelProviders.of(this, factory)
                .get(FeedListViewModel::class.java)
        feedType = arguments?.getInt("feedType") ?: LATEST_CREATED
        Log.i("feed", "type = $feedType")
        viewModel.getFeed(feedType)?.observe(viewLifecycleOwner, Observer { result ->
            if (result != null && result.isNotEmpty())
                adapter.submitList(result)
        })
        viewModel.loadFeed(feedType)

    }
}
