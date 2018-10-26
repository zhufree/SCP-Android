package info.free.scp.view.base


import android.app.Fragment
import android.content.Context


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
