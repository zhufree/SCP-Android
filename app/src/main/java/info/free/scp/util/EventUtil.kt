package info.free.scp.util

import android.content.Context
import com.umeng.analytics.MobclickAgent
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/9/21.
 *
 */

object EventUtil {

    const val switchToScpSeries = "event_1"
    const val switchToScpLibrary = "event_2"

    fun onEvent(context: Context? = ScpApplication.context, event: String) {
        MobclickAgent.onEvent(context, event)
    }
}