package info.free.scp.view.home


import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 首页，一级子页面是SCP系列和SCP图书馆两个
 */
class HomeFragment : Fragment() {
    private var listener: CategoryListener? = null
    private val seriesFragment = SeriesFragment.newInstance()
    private val libraryFragment = LibraryFragment.newInstance()
    private var fragmentList = arrayOf(seriesFragment, libraryFragment).toList()
    private var titleList = arrayOf("SCP系列", "SCP图书馆").toList()

    // TODO: Rename and change types of parameters
//    private var mParam1: String? = null
//    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (arguments != null) {
//            mParam1 = arguments!!.getString(ARG_PARAM1)
//            mParam2 = arguments!!.getString(ARG_PARAM2)
//        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CategoryListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seriesFragment.listener = listener
        val todoViewPagerAdapter = HomeFragmentPager(fragmentManager, fragmentList, titleList)
        vpHome.adapter = todoViewPagerAdapter
        tabHome.setupWithViewPager(vpHome)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
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
        // TODO: Rename and change types and number of parameters
//        fun newInstance(param1: String, param2: String): HomeFragment {
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
            return fragment
        }
    }

    interface CategoryListener {
        fun onCategoryClick(type: Int)
    }

} // Required empty public constructor
