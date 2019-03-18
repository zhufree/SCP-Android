package info.free.scp.view.feed

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import info.free.scp.databinding.SubFeedFragmentBinding
import info.free.scp.util.InjectorUtils

class SubFeedFragment : Fragment() {

    companion object {
        fun newInstance() = SubFeedFragment()
    }

    private lateinit var viewModel: FeedListViewModel

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.sub_feed_fragment, container, false)
//    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = SubFeedFragmentBinding.inflate(inflater, container, false)
        val feedAdapter = FeedAdapter()
        binding.rvFeed.adapter = feedAdapter
        subscribeUi(feedAdapter)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(FeedListViewModel::class.java)
//        refresh()
    }

    private fun subscribeUi(adapter: FeedAdapter) {
        val factory = InjectorUtils.provideFeedListViewModelFactory(requireContext())
        viewModel = ViewModelProviders.of(this, factory)
                .get(FeedListViewModel::class.java)

        viewModel.getFeed()?.observe(viewLifecycleOwner, Observer { result ->
            if (result != null && result.isNotEmpty())
                adapter.submitList(result)
        })
        viewModel.loadFeed()
    }

    fun refresh() {
        viewModel.loadFeed()
    }

}
