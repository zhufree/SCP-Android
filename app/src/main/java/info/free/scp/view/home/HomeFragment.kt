package info.free.scp.view.home


import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.search.SearchActivity
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 首页，一级子页面是SCP系列和SCP图书馆两个
 */
class HomeFragment : BaseFragment() {
    private var listener: CategoryListener? = null
    private val seriesFragment = SeriesFragment.newInstance()
    private val libraryFragment = LibraryFragment.newInstance()
    private var fragmentList = arrayOf(seriesFragment, libraryFragment).toList()
    private var titleList = arrayOf("SCP系列", "SCP图书馆").toList()

//    private var mParam1: String? = null
//    private var mParam2: String? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seriesFragment.listener = listener
        libraryFragment.listener = listener
        val scpPagerAdapter = HomeFragmentPager(childFragmentManager, fragmentList, titleList)
        vpHome?.adapter = scpPagerAdapter
        tabHome?.setupWithViewPager(vpHome)

        vpHome?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                EventUtil.onEvent(null, if (position == 0) EventUtil.showScpSeries
                        else EventUtil.showScpLibrary)
            }
        })
        category_toolbar?.setTitle(R.string.app_name)
        category_toolbar?.inflateMenu(R.menu.home_fragment_menu) //设置右上角的填充菜单
        category_toolbar?.setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.cn_page -> {
                    libraryFragment.changePage()
                }
                R.id.search -> {
                    EventUtil.onEvent(activity, EventUtil.clickSearch)
                    activity?.startActivity(Intent(activity, SearchActivity::class.java))
                }
            }
            true
        }

    }

    fun refreshTheme() {
        tabHome?.setBackgroundColor(ThemeUtil.containerBg)
        category_toolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        seriesFragment.refreshTheme()
        libraryFragment.refreshTheme()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
