package info.free.scp.view.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.CategoryFragment
import info.free.scp.view.category.ScpListFragment
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * UI新版本
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : BaseFragment() {
    var fragmentList = arrayListOf<BaseFragment>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentList = arrayListOf(
                HomePageFragment.newInstance(),
                ScpListFragment.newInstance(SCPConstants.Entry.LIBRARY_DOC, 0, ""),
                CategoryFragment.newInstance(SCPConstants.Entry.INTERNATIONAL_DOC, 0),
                CategoryFragment.newInstance(SCPConstants.Entry.GOI_DOC, 0),
                CategoryFragment.newInstance(SCPConstants.Entry.ART_DOC, 0),
                ScpListFragment.newInstance(SCPConstants.Entry.INFORMATION_DOC, 0, "")
        )

        val titleList = arrayListOf("首页", "图书馆", "SCP国际版", "GOI格式", "艺术作品", "背景资料与指导") // 单页放滑动分页
        val homePagerAdapter = TabFragmentPager(childFragmentManager, fragmentList, titleList)
        vp_home?.adapter = homePagerAdapter
        tab_home?.setupWithViewPager(vp_home)
        tab_home?.tabMode = TabLayout.MODE_SCROLLABLE
    }


    override fun refreshTheme() {
        super.refreshTheme()

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            return fragment
        }
    }
}
