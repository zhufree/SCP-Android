package info.free.scp.view.base


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import info.free.scp.util.ThemeUtil
import info.free.scp.view.category.SeriesDocActivity


/**
 * A simple [Fragment] base class.
 */
abstract class BaseFragment : Fragment() {
    var mContext: Context? = null


    override fun onAttach(context: Context?) {
        mContext = context
        super.onAttach(context)
    }

    fun goToDocPage(entry_type: Int) {
        val intent = Intent(mContext, SeriesDocActivity::class.java)
        intent.putExtra("entry_type", entry_type)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshTheme()
    }

    open fun refreshTheme() {
        view?.setBackgroundColor(ThemeUtil.containerBg)
    }
} // Required empty public constructor
