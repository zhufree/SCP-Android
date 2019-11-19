package info.free.scp.view.home


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants
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
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


/**
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : BaseFragment() {

//    private var mParam1: String? = null
//    private var mParam2: String? = null


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
        ScpDatabase.getInstance()?.let {
            startActivity<SeriesDocActivity>("entry_type" to entry_type)
        }?:run {
            startActivity<DownloadActivity>()
        }
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

} // Required empty public constructor
