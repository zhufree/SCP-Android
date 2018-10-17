package info.free.scp.view.home


import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.R
import info.free.scp.SCPConstants.SERIES
import info.free.scp.SCPConstants.SERIES_ABOUT
import info.free.scp.SCPConstants.SERIES_ARCHIVED
import info.free.scp.SCPConstants.SERIES_CN
import info.free.scp.SCPConstants.SERIES_STORY
import info.free.scp.util.EventUtil
import info.free.scp.util.EventUtil.clickAbout
import info.free.scp.util.EventUtil.clickArchived
import info.free.scp.util.EventUtil.clickSeries
import info.free.scp.util.EventUtil.clickSeriesCn
import info.free.scp.util.EventUtil.clickSeriesStory
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_series.view.*
import info.free.scp.view.home.HomeFragment.CategoryListener


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (arguments != null) {
//            mParam1 = arguments!!.getString(ARG_PARAM1)
//            mParam2 = arguments!!.getString(ARG_PARAM2)
//        }
    }

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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.tvScpSeries?.setOnClickListener {
            EventUtil.onEvent(mContext, clickSeries)
            listener?.onCategoryClick(SERIES)
        }
        view?.tvSeriesCn?.setOnClickListener{
            EventUtil.onEvent(mContext, clickSeriesCn)
            listener?.onCategoryClick(SERIES_CN)
        }
        view?.tvSeriesStory?.setOnClickListener{
            EventUtil.onEvent(mContext, clickSeriesStory)
            listener?.onCategoryClick(SERIES_STORY)
        }
        view?.tvSeriesArchived?.setOnClickListener{
            EventUtil.onEvent(mContext, clickArchived)
            listener?.onCategoryClick(SERIES_ARCHIVED)
        }
        view?.tvSeriesAbout?.setOnClickListener{
            EventUtil.onEvent(mContext, clickAbout)
            listener?.onCategoryClick(SERIES_ABOUT)
        }
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
