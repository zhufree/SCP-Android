package info.free.scp.view.feed


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import info.free.scp.R
import info.free.scp.SCPConstants.TOP_RATED_ALL
import info.free.scp.SCPConstants.TOP_RATED_GOI
import info.free.scp.SCPConstants.TOP_RATED_SCP
import info.free.scp.SCPConstants.TOP_RATED_TALES
import info.free.scp.SCPConstants.TOP_RATED_WANDERS
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_top_rated.*
import org.jetbrains.anko.support.v4.startActivity

/**
 * A simple [Fragment] subclass.
 * Use the [TopRatedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TopRatedFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_top_rated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cv_top_rated_all.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_ALL)
        }
        cv_top_rated_scp?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_SCP)
        }
        cv_top_rated_tales?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_TALES)
        }
        cv_top_rated_goi?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_GOI)
        }
        cv_top_rated_wanderer?.setOnClickListener {
            startActivity<TopRatedListActivity>("feedType" to TOP_RATED_WANDERS)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TopRatedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
                TopRatedFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}
