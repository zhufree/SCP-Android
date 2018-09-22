package info.free.scp.util

import android.content.Context
import com.umeng.analytics.MobclickAgent
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/9/21.
 *
 */

object EventUtil {

    const val showScpSeries = "event_1"
    const val showScpLibrary = "event_2"
    const val clickSeries = "event_3"
    const val clickSeriesCn = "event_4"
    const val clickSeriesStory = "event_5"
    const val clickArchived = "event_6"
    const val clickAbout = "event_7"
    const val clickTales = "event_8"
    const val clickTalesCn = "event_9"
    const val clickStory = "event_12"
    const val clickStoryCn = "event_13"
    const val clickSettings = "event_10"
    const val clickSettingsCn = "event_11"
    const val clickContest = "event_14"
    const val clickContestCn = "event_15"
    const val clickEvent = "event_16"
    const val clickTalesCnByTime = "event_17"

    fun onEvent(context: Context? = ScpApplication.context, event: String) {
        MobclickAgent.onEvent(context, event)
    }
}