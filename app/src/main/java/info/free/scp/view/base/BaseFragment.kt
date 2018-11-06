package info.free.scp.view.base


import android.content.Context
import android.support.v4.app.Fragment


/**
 * A simple [Fragment] base class.
 */
abstract class BaseFragment : Fragment() {
    var mContext : Context? = null


    override fun onAttach(context: Context?) {
        mContext = context
        super.onAttach(context)
    }
} // Required empty public constructor
