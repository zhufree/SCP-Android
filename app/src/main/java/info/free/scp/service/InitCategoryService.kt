package info.free.scp.service

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import info.free.scp.bean.ScpModel
import info.free.scp.util.PreferenceUtil
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.SCPConstants.BroadCastAction.INIT_PROGRESS
import info.free.scp.SCPConstants.SaveType.SAVE_ABOUT
import info.free.scp.SCPConstants.SaveType.SAVE_ARCHIVED
import info.free.scp.SCPConstants.SaveType.SAVE_CONTEST
import info.free.scp.SCPConstants.SaveType.SAVE_CONTEST_CN
import info.free.scp.SCPConstants.SaveType.SAVE_DECOMMISSIONED
import info.free.scp.SCPConstants.SaveType.SAVE_EVENT
import info.free.scp.SCPConstants.SaveType.SAVE_EX
import info.free.scp.SCPConstants.SaveType.SAVE_JOKE
import info.free.scp.SCPConstants.SaveType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.SaveType.SAVE_REMOVED
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_CN
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_STORY_1
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_STORY_2
import info.free.scp.SCPConstants.SaveType.SAVE_SERIES_STORY_3
import info.free.scp.SCPConstants.SaveType.SAVE_SETTINGS
import info.free.scp.SCPConstants.SaveType.SAVE_SETTINGS_CN
import info.free.scp.SCPConstants.SaveType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.SaveType.SAVE_STORY_SERIES_CN
import info.free.scp.SCPConstants.SaveType.SAVE_TALES_BY_TIME
import info.free.scp.SCPConstants.SaveType.SAVE_TALES_CN_PREFIX
import info.free.scp.SCPConstants.SaveType.SAVE_TALES_PREFIX
import info.free.scp.db.ScpDao


class InitCategoryService : IntentService("initDataService") {

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    var scpModels: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    var requestCount = 0
        set(value) {
            Log.i("loading", "requestCount = $value")
            field = value
            sendThreadStatus(3*value)
            if (value == 29) {
                ScpDao.getInstance().insertCategoryData(scpModels)
                PreferenceUtil.setInitCategoryFinish(true)
                sendThreadStatus(100)
            }
        }

    override fun onCreate() {
        super.onCreate()
        // 0-499,500-999
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    private fun sendThreadStatus(progress: Int) {
        val intent = Intent(INIT_PROGRESS)
        intent.putExtra("progress", progress)
        mLocalBroadcastManager?.sendBroadcast(intent)
    }


    override fun onHandleIntent(intent: Intent?) {
        getAllScpList(0)
    }

    private fun getAllScpList(i: Int) {
        HttpManager.instance.getAllScp(i*500, 500) {
            for ((index, scp) in it.withIndex()) {
                when (scp.requestType) {
                    "series" -> {
                        scp.saveType = if (scp.cn == "true") SAVE_SERIES_CN
                        else SAVE_SERIES
                    }
                    "joke" -> {
                        scp.saveType = if (scp.cn == "true") SAVE_JOKE_CN
                        else SAVE_JOKE
                    }
                    "archived" -> {
                        scp.saveType = SAVE_ARCHIVED
                    }
                    "ex" -> {
                        scp.saveType = SAVE_EX
                    }
                    "decommissioned" -> {
                        scp.saveType = SAVE_DECOMMISSIONED
                    }
                    "removed" -> {
                        scp.saveType = SAVE_REMOVED
                    }
                    "story" -> {
                        when (scp.storyNum) {
                            "1" -> scp.saveType = SAVE_SERIES_STORY_1
                            "2" -> scp.saveType = SAVE_SERIES_STORY_2
                            "3" -> scp.saveType = SAVE_SERIES_STORY_3
                        }
                    }
                    "story_series" -> {
                        scp.saveType = if (scp.cn == "true") SAVE_STORY_SERIES_CN
                        else SAVE_STORY_SERIES
                    }
                    "tale" -> {
                        scp.saveType = if (scp.cn == "true") SAVE_TALES_CN_PREFIX + scp.pageCode
                        else SAVE_TALES_PREFIX + scp.pageCode
                    }
                    "setting" -> {
                        scp.saveType = if (scp.cn == "true") SAVE_SETTINGS_CN
                        else SAVE_SETTINGS
                    }
                    "contest" -> {
                        scp.saveType = if (scp.cn == "true") SAVE_CONTEST_CN
                        else SAVE_CONTEST
                    }
                    "event" -> {
                        scp.saveType = SAVE_EVENT
                    }
                    "tale_by_time" -> {
                        scp.saveType = SAVE_TALES_BY_TIME
                    }
                    "about" -> {
                        scp.saveType = SAVE_ABOUT
                    }
                }
                scp.index = i*500 + index
                Log.i("loading", "${scp.saveType} index = ${scp.index}")
            }
            scpModels.addAll(it)
            Log.i("loading", "i = $i, size = ${scpModels.size}")
            requestCount += 1
            val next = i + 1
            if (next < 29) {
                getAllScpList(next)
            }
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
