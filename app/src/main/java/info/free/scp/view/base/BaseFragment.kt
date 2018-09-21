package info.free.scp.view.base


import android.app.Fragment
import com.umeng.analytics.MobclickAgent


/**
 * A simple [Fragment] base class.
 */
abstract class BaseFragment : Fragment() {


    override fun onResume() {
        super.onResume()
        MobclickAgent.onPageStart(this.javaClass.name) //统计页面，"MainScreen"为页面名称，可自定义
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPageEnd(this.javaClass.name)
    }

} // Required empty public constructor
