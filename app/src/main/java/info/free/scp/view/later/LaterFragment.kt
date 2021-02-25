package info.free.scp.view.later


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import info.free.scp.R
import info.free.scp.view.base.BaseFragment
import info.free.scp.view.home.TabFragmentPager
import kotlinx.android.synthetic.main.fragment_later.*


/**
 * A simple [Fragment] subclass.
 * Use the [LaterFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 待读和收藏tab
 */
class LaterFragment : BaseFragment() {

    //    private var mParam1: String? = null
    //    private var mParam2: String? = null
    var fragmentList = arrayListOf<BaseFragment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_later, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentList = arrayListOf(LaterListFragment.newInstance(),
                LikeBoxListFragment.newInstance())
        val titleList = arrayListOf("待读", "收藏夹")
        val laterPagerAdapter = TabFragmentPager(childFragmentManager, fragmentList, titleList)
        vp_later?.adapter = laterPagerAdapter
        tab_later?.setupWithViewPager(vp_later)
    }

    override fun refreshTheme() {
        super.refreshTheme()
        fragmentList.forEach { it.refreshTheme() }
    }

    companion object {
        fun newInstance(): LaterFragment {
            val fragment = LaterFragment()
            return fragment
        }
    }

} // Required empty public constructor
