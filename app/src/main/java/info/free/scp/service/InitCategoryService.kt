package info.free.scp.service

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import info.free.scp.bean.ScpModel
import info.free.scp.util.PreferenceUtil
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.BroadCastAction.INIT_PROGRESS
import info.free.scp.SCPConstants.SaveType.SAVE_ABNORMAL
import info.free.scp.SCPConstants.SaveType.SAVE_INFO
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
import info.free.scp.SCPConstants.SaveType.SAVE_SETTINGS
import info.free.scp.SCPConstants.SaveType.SAVE_SETTINGS_CN
import info.free.scp.SCPConstants.SaveType.SAVE_STORY_SERIES
import info.free.scp.SCPConstants.SaveType.SAVE_STORY_SERIES_CN
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
            sendThreadStatus(5*value)
        }

    override fun onCreate() {
        super.onCreate()
        // 0-499,500-999
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    private fun finishCategoryLoad() {
        ScpDao.getInstance().insertCategoryData(scpModels)
        PreferenceUtil.setInitCategoryFinish(true)
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
                scp.index = i*500 + index
                if (scp.downloadType == SCPConstants.Download.DOWNLOAD_COLLECTIONS) {
                    scp.isCollection = 1
                }
                Log.i("loading", "${scp.scpType} index = ${scp.index}")
            }
            scpModels.addAll(it)
            Log.i("loading", "i = $i, size = ${scpModels.size}")
            requestCount += 1
            val next = i + 1
            if (it.size == 500) {
                getAllScpList(next)
            } else {
                finishCategoryLoad()
            }
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
