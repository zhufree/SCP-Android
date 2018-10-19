package info.free.scp.service

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.util.PreferenceUtil
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.db.ScpDao


class InitCategoryService : IntentService("initDataService") {
    val INIT_PROGRESS = "initProgress"

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    var scpModels: MutableList<ScpModel> = emptyList<ScpModel>().toMutableList()
    var requestCount = 0
        set(value) {
            Log.i("loading", "requestCount = $value")
            field = value
            sendThreadStatus(3*value)
            if (value == 29) {
                ScpDao.getInstance().insertCategoryData(scpModels)
                PreferenceUtil.setInitDataFinish(true)
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
