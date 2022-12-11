package info.free.scp.view.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by zhufree on 2018/8/28.
 *
 */
class TabFragmentPager(
    activity: FragmentActivity,
    fragmentList: List<Fragment>,
    val itemsCount: Int
) : FragmentStateAdapter(activity) {
    private var mFragmentList: List<Fragment> = fragmentList

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

//    override fun getItem(position: Int): Fragment {
//        return mFragmentList[position]
//    }
//
//    override fun getCount(): Int {
//        return mFragmentList.size
//    }
//
//    override fun getPageTitle(position: Int): CharSequence {
//        return mTitleList[position]
//    }

}
