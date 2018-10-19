package info.free.scp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.util.Log
import info.free.scp.SCPConstants
import info.free.scp.util.PreferenceUtil
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.R
import info.free.scp.db.ScpDao


class InitDetailService : IntentService("initDataService") {
    val LOAD_DETAIL_FINISH = "loadDetailFinish"
    private var notificationManager: NotificationManager? = null
    private val DOWNLOAD_DETAIL_NOTIFICATION = R.string.download_detail_service
    val channelID = "1"

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    override fun onCreate() {
        super.onCreate()
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
    }


    override fun onHandleIntent(intent: Intent?) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotification(PreferenceUtil.getDetailDataLoadCount()*3)
        getDetail(PreferenceUtil.getDetailDataLoadCount())
    }

    @RequiresApi(api = 26)
    private fun createNotifyChannel() {
        val channelName = "channel_name"
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager?.createNotificationChannel(channel)
        Notification.Builder(this, channel.id)
    }

    /**
     * Notification
     */
    private fun createNotification(progress: Int) {

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            createNotifyChannel()
            Notification.Builder(this, channelID)
        } else Notification.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
                //设置通知栏横条的图标
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round))
                .setSmallIcon(R.mipmap.ic_launcher)
                // 立即启动
                .setWhen(System.currentTimeMillis())
                // 内容标题
                .setContentTitle(getString(R.string.download_detail_service))
                // 内容文字
                .setContentText("下载进度：$progress/100")
                // 不会被滑动删除（常驻）
                .setOngoing(true)
                // 设置点击intent
        //设置通知栏的标题内容
        //创建通知
        val notification = builder.build()
        //设置为前台服务
        notificationManager?.notify(DOWNLOAD_DETAIL_NOTIFICATION, notification)
        if (progress == 100) {
            notificationManager?.cancel(DOWNLOAD_DETAIL_NOTIFICATION)
        }
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
            val next = i + 1
            if (next < 29) {
                createNotification(next*3)
                getDetail(next)
            } else {
                createNotification(100)
                notifyLoadFinish()
            }
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
