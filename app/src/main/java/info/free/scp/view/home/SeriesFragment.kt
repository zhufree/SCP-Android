package info.free.scp.view.home


import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.Category.SERIES_ABOUT
import info.free.scp.SCPConstants.Category.SERIES_ARCHIVED
import info.free.scp.SCPConstants.Category.SERIES_CN
import info.free.scp.SCPConstants.Category.SERIES_STORY
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.ThemeUtil
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.home.HomeFragment.CategoryListener
import kotlinx.android.synthetic.main.fragment_series.*


/**
 * A simple [Fragment] subclass.
 * Use the [SeriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 首页，一级子页面是SCP系列和SCP图书馆两个
 */
class SeriesFragment : BaseFragment() {
    var listener: CategoryListener? = null

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
        return inflater.inflate(R.layout.fragment_series, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvScpSeries?.background?.alpha = 90
        tvSeriesCn?.background?.alpha = 90
        tvSeriesStory?.background?.alpha = 90
        tvSeriesArchived?.background?.alpha = 90
        tvSeriesAbout?.background?.alpha = 90
        tv_random?.background?.alpha = 90
        tvScpSeries?.setOnClickListener {
            listener?.onCategoryClick(SERIES)
        }
        tvSeriesCn?.setOnClickListener{
            listener?.onCategoryClick(SERIES_CN)
        }
        tvSeriesStory?.setOnClickListener{
            listener?.onCategoryClick(SERIES_STORY)
        }
        tvSeriesArchived?.setOnClickListener{
            listener?.onCategoryClick(SERIES_ARCHIVED)
        }
        tvSeriesAbout?.setOnClickListener{
            listener?.onCategoryClick(SERIES_ABOUT)
        }
        tv_random?.setOnClickListener {
            EventUtil.onEvent(activity, EventUtil.clickRandom)
            PreferenceUtil.addPoints(2)
            activity?.startActivity(Intent(activity, DetailActivity::class.java))
        }
    }

    fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
        tvScpSeries?.setTextColor(ThemeUtil.darkText)
        tvSeriesCn?.setTextColor(ThemeUtil.darkText)

        tvSeriesStory?.setTextColor(ThemeUtil.darkText)
        tvSeriesArchived?.setTextColor(ThemeUtil.darkText)
        tvSeriesAbout?.setTextColor(ThemeUtil.darkText)
        tv_random?.setTextColor(ThemeUtil.darkText)
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
        fun newInstance(): SeriesFragment {
            val fragment = SeriesFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
            return fragment
        }
    }

} // Required empty public constructor
