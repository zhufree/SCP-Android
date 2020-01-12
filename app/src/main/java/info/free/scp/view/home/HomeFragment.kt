package info.free.scp.view.home


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.ScpApplication
import info.free.scp.db.ScpDatabase
import info.free.scp.util.FileUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.util.UpdateManager
import info.free.scp.view.base.BaseActivity
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.SeriesDocActivity
import info.free.scp.view.download.DownloadActivity
import info.free.scp.view.search.SearchActivity
import info.free.scp.view.user.LaterAndHistoryActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.selector
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


/**
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        home_toolbar?.setTitle(R.string.title_scp_documents)
        home_toolbar?.inflateMenu(R.menu.home_fragment_menu) //设置右上角的填充菜单
        home_toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    activity?.startActivity(Intent(activity, SearchActivity::class.java))
                }
            }
            true
        }
        tv_home_notice?.text = PreferenceUtil.getNotice()
        tv_series_doc?.background?.alpha = 50
        tv_story_doc?.background?.alpha = 50
        tv_about_doc?.background?.alpha = 50
        tv_read_later?.background?.alpha = 50
        tv_joke_doc?.background?.alpha = 50
        tv_direct?.background?.alpha = 50
        tv_series_doc?.setOnClickListener {
            goToDocPage(SCPConstants.Entry.SCP_DOC)
        }
        tv_story_doc?.setOnClickListener {
            goToDocPage(SCPConstants.Entry.STORY_DOC)
        }
        tv_about_doc?.setOnClickListener {
            goToDocPage(SCPConstants.Entry.ABOUT_SCP_DOC)
        }
        tv_read_later?.setOnClickListener {
            startActivity<LaterAndHistoryActivity>("view_type" to LATER_TYPE)
        }
        tv_joke_doc?.setOnClickListener {
            goToDocPage(SCPConstants.Entry.JOKE_DOC)
        }
        tv_direct?.setOnClickListener {
            PreferenceUtil.addPoints(2)
            startActivity<DirectActivity>()
        }
    }

    private fun goToDocPage(entry_type: Int) {
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
    }

    override fun refreshTheme() {
        super.refreshTheme()
        home_toolbar?.setBackgroundColor(ThemeUtil.toolbarBg)
        cd_notice_container?.setBackgroundColor(ThemeUtil.containerBg)
        tv_home_notice?.setTextColor(ThemeUtil.darkText)
        tv_series_doc?.setTextColor(ThemeUtil.darkText)
        tv_story_doc?.setTextColor(ThemeUtil.darkText)

        tv_about_doc?.setTextColor(ThemeUtil.darkText)
        tv_read_later?.setTextColor(ThemeUtil.darkText)
        tv_joke_doc?.setTextColor(ThemeUtil.darkText)
        tv_direct?.setTextColor(ThemeUtil.darkText)
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
