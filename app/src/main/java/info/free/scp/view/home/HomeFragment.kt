package info.free.scp.view.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.db.ScpDatabase
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.CategoryFragment
import info.free.scp.view.category.SeriesDocActivity
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.feed.SubFeedFragment
import info.free.scp.view.feed.TopRatedFragment
import info.free.scp.view.random.RandomActivity
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.yesButton


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
        fragmentList = arrayListOf(HomePageFragment.newInstance(),
                SubFeedFragment.newInstance(SCPConstants.LATEST_TRANSLATED), TopRatedFragment.newInstance())

        val titleList = arrayListOf("首页", "图书馆", "SCP国际版", "背景资料与指导") // 单页放滑动分页
        val homePagerAdapter = TabFragmentPager(childFragmentManager, fragmentList, titleList)
        vp_home?.adapter = homePagerAdapter
        tab_home?.setupWithViewPager(vp_home)

    }

    private fun goToDocPage(entry_type: Int) {
        if (!PreferenceUtil.getShownModeNotice()) {
            alert("为了方便用户尽快开始阅读，0.1.5版本添加了全局在线阅读模式，如果需要下载数据库并使用离线模式，" +
                    "请在【我的-数据下载及配置】中设置.\n注意：1.之前已离线的用户仍将使用离线模式，新用户默认使用在线模式" +
                    "\n2.在线阅读模式的数据仍和数据库相同，并非和官网完全同步，具体同步日期可在离线页面查看。\n" +
                    "3.正文页面菜单中的在线模式是强制访问网页，和全局阅读模式不同") {
                positiveButton("我知道了") {
                    ScpDatabase.getInstance()?.let {
                        PreferenceUtil.setAppMode(OFFLINE)
                    } ?: run {
                        PreferenceUtil.setAppMode(ONLINE)
                    }
                    startActivity<SeriesDocActivity>("entry_type" to entry_type)
                }
            }.show()
            PreferenceUtil.setShownModeNotice()
        } else {
            startActivity<SeriesDocActivity>("entry_type" to entry_type)
        }
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
