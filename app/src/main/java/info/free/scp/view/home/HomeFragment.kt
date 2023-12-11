package info.free.scp.view.home


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.databinding.FragmentHomeBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.CategoryFragment
import info.free.scp.view.category.ScpListFragment


/**
 * UI新版本
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : BaseFragment() {
    var fragmentList = arrayListOf<BaseFragment>()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
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

        val titleList = arrayListOf(
            "首页", "图书馆",
            "SCP国际版", "GOI格式", "艺术作品",
            "背景资料与指导"
        ) // 单页放滑动分页
        val homePagerAdapter =
            this.activity?.let { TabFragmentPager(it, fragmentList, fragmentList.size) }
        binding.vpHome.adapter = homePagerAdapter
        binding.vpHome.offscreenPageLimit = 3
        TabLayoutMediator(binding.tabHome, binding.vpHome) { tab, position ->
            tab.text = titleList[position]
        }.attach()
        binding.tabHome.tabMode = TabLayout.MODE_SCROLLABLE
    }


    override fun refreshTheme() {
        super.refreshTheme()
        if (_binding == null) {
            binding.tabHome.background = ColorDrawable(ThemeUtil.itemBg)
            binding.tabHome.setSelectedTabIndicatorColor(ThemeUtil.accentColor)
            binding.tabHome.setTabTextColors(ThemeUtil.mediumText, ThemeUtil.accentColor)
            fragmentList.forEach { it.refreshTheme() }
        }
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
