package info.free.scp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import info.free.scp.SCPConstants
import info.free.scp.db.ScpDao

class InitDataService : Service() {

    override fun onCreate() {
        super.onCreate()
        for (i in 0 .. 10) {
            // 0-499,500-999
            Log.i("loading", "i = $i")
            HttpManager.instance.getSeries("{\"cn\":\"false\"}",
                    i*500, 500) {
                for ((index, scp) in it.withIndex()) {
                    scp.saveType = SCPConstants.SERIES
                    scp.index = i*500 + index
                    ScpDao.getInstance().replaceScpModel(scp)
                }
            }
            HttpManager.instance.getSeries("{\"cn\":\"true\"}",
                    i*100, 100) {
                for ((index, scp) in it.withIndex()) {
                    scp.saveType = SCPConstants.SERIES
                    scp.index = i*100 + index
                    Log.i("loading", "index = ${scp.index}")
                    ScpDao.getInstance().replaceScpModel(scp)
                }
            }
        }
        for (i in 0 .. 5) {
            HttpManager.instance.getStory("{\"story_num\":\"1\"}",start, limit) {
                scpList.addAll(it)
                for (scp in it) {
                    ScpDao.getInstance().replaceScpModel(scp)
                }
                scpAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
