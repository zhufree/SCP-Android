package info.free.scp.view.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by zhufree on 2018/8/28.
 *
 */
class HomeFragmentPager(fragmentManager: FragmentManager, fragmentList: List<Fragment>,
                        titleList: List<String>) : FragmentPagerAdapter(fragmentManager) {
    private var mFragmentList: List<Fragment> = fragmentList
    private var mTitleList: List<String> = titleList
    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitleList[position]
    }

}
