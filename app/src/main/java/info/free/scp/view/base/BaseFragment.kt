package info.free.scp.view.base


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import info.free.scp.util.ThemeUtil


/**
 * A simple [Fragment] base class.
 */
abstract class BaseFragment : Fragment() {
    var mContext: Context? = null


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshTheme()
    }

    open fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
    }
}
