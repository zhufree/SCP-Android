package info.free.scp.view.home


import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.Category.CONTEST
import info.free.scp.SCPConstants.Category.CONTEST_CN
import info.free.scp.SCPConstants.Category.EVENT
import info.free.scp.SCPConstants.Category.SETTINGS
import info.free.scp.SCPConstants.Category.SETTINGS_CN
import info.free.scp.SCPConstants.Category.STORY_SERIES
import info.free.scp.SCPConstants.Category.STORY_SERIES_CN
import info.free.scp.SCPConstants.Category.TALES
import info.free.scp.SCPConstants.Category.TALES_BY_TIME
import info.free.scp.SCPConstants.Category.TALES_CN
import info.free.scp.ScpApplication
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.category.SeriesDocActivity
import info.free.scp.view.home.HomeFragment.CategoryListener
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.android.synthetic.main.fragment_library.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [LibraryFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 首页，一级子页面是SCP系列和SCP图书馆两个
 */
class LibraryFragment : BaseFragment() {
    var listener: CategoryListener? = null
    private var isCnPage = false // 是否是cn页面

    //    private var mParam1: String? = null
    //    private var mParam2: String? = null

    override fun onAttach(context: Context?) {
        mContext = context
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
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_joke_doc?.background?.alpha = 90
        tv_more_about?.background?.alpha = 90
        tv_settings_doc?.background?.alpha = 90
        tvEssayContest?.background?.alpha = 90
        tvEventRecord?.background?.alpha = 90
    }

    fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
        tv_joke_doc?.setTextColor(ThemeUtil.darkText)
        tv_more_about?.setTextColor(ThemeUtil.darkText)
        tv_settings_doc?.setTextColor(ThemeUtil.darkText)
        tvEssayContest?.setTextColor(ThemeUtil.darkText)
        tvEventRecord?.setTextColor(ThemeUtil.darkText)
        tvMore?.setTextColor(ThemeUtil.darkText)
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
        fun newInstance(): LibraryFragment {
            val fragment = LibraryFragment()
            //            val args = Bundle()
            //            args.putString(ARG_PARAM1, param1)
            //            args.putString(ARG_PARAM2, param2)
            //            fragment.arguments = args
            return fragment
        }
    }
} // Required empty public constructor
