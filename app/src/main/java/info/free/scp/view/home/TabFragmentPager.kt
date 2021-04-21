package info.free.scp.view.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Created by zhufree on 2018/8/28.
 *
 */
class TabFragmentPager(fragmentManager: FragmentManager, fragmentList: List<Fragment>,
                       titleList: List<String>) : FragmentStatePagerAdapter(fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var mFragmentList: List<Fragment> = fragmentList
    private var mTitleList: List<String> = titleList

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mTitleList[position]
    }

}
