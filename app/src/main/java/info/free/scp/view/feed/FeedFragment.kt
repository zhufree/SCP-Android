package info.free.scp.view.feed


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import info.free.scp.R
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.SCPConstants.LATEST_TRANSLATED
import info.free.scp.databinding.FragmentFeedBinding
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager


/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 最新，包括最新，和评分三个部分
 */
class FeedFragment : BaseFragment() {

    var fragmentList = arrayListOf<BaseFragment>()

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentList = arrayListOf(
            SubFeedFragment.newInstance(LATEST_CREATED),
            SubFeedFragment.newInstance(LATEST_TRANSLATED)
        )
        val titleList = arrayListOf("最近原创", "最近翻译")
        val feedPagerAdapter =
            this.activity?.let { TabFragmentPager(it, fragmentList, fragmentList.size) }
        binding.vpFeed.adapter = feedPagerAdapter
        TabLayoutMediator(binding.tabFeed, binding.vpFeed) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    override fun refreshTheme() {
        super.refreshTheme()
        fragmentList.forEach { it.refreshTheme() }
    }

    companion object {
        fun newInstance(): FeedFragment {
            val fragment = FeedFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
            return fragment
        }
    }

} // Required empty public constructor
