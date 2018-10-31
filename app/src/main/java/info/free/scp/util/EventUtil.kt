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
    const val clickAboutApp = "event_18"
    const val clickAboutMe = "event_19"
    const val clickChangeTheme = "event_20"
    const val clickInitData = "event_21"
    const val clickResetData = "event_22"
    const val clickChangeReadMode = "event_23"
    const val clickSearch = "event_24"
    const val doSearch = "event_25"
    const val clickRandom = "event_26"
    const val clickLastArticle = "event_27"
    const val clickNextArticle = "event_28"
    const val clickArticleRandom = "event_29"
    const val clickDonation = "event_30"
    const val clickDetail = "event_31"
    const val finishDetail = "event_32"
    const val clickLike = "event_33"
    const val clickLikeList = "event_34"
    const val clickOpenInBrowser = "event_35"
    const val clickCopyLink = "event_36"

    fun onEvent(context: Context? = ScpApplication.context, event: String) {
        MobclickAgent.onEvent(context, event)
    }
    fun onEvent(context: Context? = ScpApplication.context, event: String, arg: String) {
        MobclickAgent.onEvent(context, event, arg)
    }
}