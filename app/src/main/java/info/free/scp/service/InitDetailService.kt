package info.free.scp.service

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import info.free.scp.SCPConstants
import info.free.scp.util.PreferenceUtil
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.db.ScpDao


class InitDetailService : IntentService("initDataService") {
    val LOAD_DETAIL_FINISH = "loadDetailFinish"

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    override fun onCreate() {
        super.onCreate()
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
    }


    override fun onHandleIntent(intent: Intent?) {
        getDetail(PreferenceUtil.getDetailDataLoadCount())
    }

    private fun getDetail(i: Int) {
        HttpManager.instance.getDetail(i*500, 500) {
            for ((index, scp) in it.withIndex()) {
                when (scp.requestType) {
                    "series" -> {
                        scp.saveType = if (scp.cn == "true") SCPConstants.SAVE_SERIES_CN
                        else SCPConstants.SAVE_SERIES
                    }
                    "joke" -> {
                        scp.saveType = if (scp.cn == "true") SCPConstants.SAVE_JOKE_CN
                        else SCPConstants.SAVE_JOKE
                    }
                    "archived" -> {
                        scp.saveType = SCPConstants.SAVE_ARCHIVED
                    }
                    "ex" -> {
                        scp.saveType = SCPConstants.SAVE_EX
                    }
                    "decommissioned" -> {
                        scp.saveType = SCPConstants.SAVE_DECOMMISSIONED
                    }
                    "removed" -> {
                        scp.saveType = SCPConstants.SAVE_REMOVED
                    }
                    "story" -> {
                        when (scp.storyNum) {
                            "1" -> scp.saveType = SCPConstants.SAVE_SERIES_STORY_1
                            "2" -> scp.saveType = SCPConstants.SAVE_SERIES_STORY_2
                            "3" -> scp.saveType = SCPConstants.SAVE_SERIES_STORY_3
                        }
                    }
                    "story_series" -> {
                        scp.saveType = if (scp.cn == "true") SCPConstants.SAVE_STORY_SERIES_CN
                        else SCPConstants.SAVE_STORY_SERIES
                    }
                    "tale" -> {
                        scp.saveType = if (scp.cn == "true") SCPConstants.SAVE_TALES_CN_PREFIX + scp.pageCode
                        else SCPConstants.SAVE_TALES_PREFIX + scp.pageCode
                    }
                    "setting" -> {
                        scp.saveType = if (scp.cn == "true") SCPConstants.SAVE_SETTINGS_CN
                        else SCPConstants.SAVE_SETTINGS
                    }
                    "contest" -> {
                        scp.saveType = if (scp.cn == "true") SCPConstants.SAVE_CONTEST_CN
                        else SCPConstants.SAVE_CONTEST
                    }
                    "event" -> {
                        scp.saveType = SCPConstants.SAVE_EVENT
                    }
                    "tale_by_time" -> {
                        scp.saveType = SCPConstants.SAVE_TALES_BY_TIME
                    }
                    "about" -> {
                        scp.saveType = SCPConstants.SAVE_ABOUT
                    }
                }
                scp.index = i*500 + index
                Log.i("loading", "${scp.saveType} index = ${scp.index}")
            }
            ScpDao.getInstance().insertDetailData(it)
            PreferenceUtil.addDetailDataLoadCount()
            notifyLoadFinish()
        }
    }

    private fun notifyLoadFinish() {
        val intent = Intent(LOAD_DETAIL_FINISH)
        mLocalBroadcastManager?.sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
