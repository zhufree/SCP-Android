package info.free.scp.view.home


import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import info.free.scp.R
import info.free.scp.SCPConstants.SCP_CONTEST
import info.free.scp.SCPConstants.SCP_CONTEST_CN
import info.free.scp.SCPConstants.SCP_EVENT
import info.free.scp.SCPConstants.SCP_SETTINGS
import info.free.scp.SCPConstants.SCP_SETTINGS_CN
import info.free.scp.SCPConstants.SCP_STORY_SERIES
import info.free.scp.SCPConstants.SCP_STORY_SERIES_CN
import info.free.scp.SCPConstants.SCP_TALES
import info.free.scp.SCPConstants.SCP_TALES_BY_TIME
import info.free.scp.SCPConstants.SCP_TALES_CN
import info.free.scp.ScpApplication
import info.free.scp.view.base.BaseFragment
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

    // TODO: Rename and change types of parameters
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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.tvScpTales?.setOnClickListener {
            listener?.onCategoryClick(if (isCnPage) SCP_TALES_CN else SCP_TALES)
        }
        view?.tvStorySeries?.setOnClickListener {
            listener?.onCategoryClick(if (isCnPage) SCP_STORY_SERIES_CN else SCP_STORY_SERIES)
        }
        view?.tvSettings?.setOnClickListener {
            listener?.onCategoryClick(if (isCnPage) SCP_SETTINGS_CN else SCP_SETTINGS)
        }
        view?.tvEssayContest?.setOnClickListener {
            listener?.onCategoryClick(if (isCnPage) SCP_CONTEST_CN else SCP_CONTEST)
        }
        view?.tvEventRecord?.setOnClickListener {
            listener?.onCategoryClick(SCP_EVENT)
        }
        view?.tvMore?.setOnClickListener {
            if (isCnPage) {
                listener?.onCategoryClick(SCP_TALES_BY_TIME)
            } else {
                Toast.makeText(ScpApplication.context, "更多功能敬请期待", LENGTH_SHORT).show()
            }
        }
    }

    fun changePage() {
        isCnPage = !isCnPage
        if (isCnPage) {
            tvScpTales.setText(R.string.title_scp_tales_cn)
            tvSettings.setText(R.string.title_scp_settings_cn)
            tvStorySeries.setText(R.string.title_scp_story_series_cn)
            tvEssayContest.setText(R.string.title_scp_essay_cn)
            tvEventRecord.setText(R.string.title_scp_event)
            tvMore.setText(R.string.title_scp_story_by_time)
        } else {
            tvScpTales.setText(R.string.title_scp_tales)
            tvSettings.setText(R.string.title_scp_settings)
            tvStorySeries.setText(R.string.title_scp_story_series)
            tvEssayContest.setText(R.string.title_scp_essay)
            tvEventRecord.setText(R.string.title_scp_event)
            tvMore.setText(R.string.title_more_function)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
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
        // TODO: Rename and change types and number of parameters
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
