package info.free.scp.view.feed


import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.SCPConstants.LATEST_TRANSLATED
import info.free.scp.service.HttpManager
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager
import kotlinx.android.synthetic.main.fragment_feed.*


/**
 * A simple [Fragment] subclass.
 * Use the [FeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 最新，包括最新，和评分三个部分
 */
class FeedFragment : BaseFragment() {

//    private var mParam1: String? = null
//    private var mParam2: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentList = arrayListOf(SubFeedFragment.newInstance(LATEST_CREATED),
                SubFeedFragment.newInstance(LATEST_TRANSLATED))
        val titleList = arrayListOf("最近原创", "最近翻译")
        val feedPagerAdapter = TabFragmentPager(childFragmentManager, fragmentList, titleList)
        vp_feed?.adapter = feedPagerAdapter
        tab_feed?.setupWithViewPager(vp_feed)
//        HttpManager.instance.getLatest {
//            Log.i("feed", it.toString())
//        }
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//        private val LISTENER = "listener"
//        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
//        fun newInstance(param1: String, param2: String): HomeFragment {
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
