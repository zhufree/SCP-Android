package info.free.scp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import info.free.scp.util.PreferenceUtil
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.R
import info.free.scp.db.ScpDao
import info.free.scp.util.Utils


class InitDetailService : IntentService("initDataService") {
    val LOAD_DETAIL_FINISH = "loadDetailFinish"
    private var notificationManager: NotificationManager? = null
    private val DOWNLOAD_DETAIL_NOTIFICATION = R.string.download_detail_service
    val channelID = "zhufree"
    private var downloadList = emptyList<Int>().toMutableList()
    private var isDownloading = false

    private var mLocalBroadcastManager: LocalBroadcastManager? = null

    override fun onCreate() {
        super.onCreate()
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
    }


    override fun onHandleIntent(intent: Intent?) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val downloadType = intent?.getIntExtra("download_type", 0)
        downloadType?.let {
            downloadList.add(it)
            // 检查之前的进度
            val downloadCount = PreferenceUtil.getDetailDataLoadCount(it)
            if (downloadCount == 0) {
                // 如果没有进度，标记为没有离线完
                PreferenceUtil.setDetailDataLoadFinish(it, false)
            }
            if (!isDownloading) {
                isDownloading = true
                createNotification(it, downloadCount * 500)
                // 从之前的进度开始下载
                getPartDetail(it, downloadCount)
            }
        }
    }

    @RequiresApi(api = 26)
    private fun createNotifyChannel() {
        val channelName = "channel_name"
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.setSound(null, null)
        notificationManager?.createNotificationChannel(channel)
    }

    /**
     * Notification
     */
    private fun createNotification(downloadType: Int, count: Int) {

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
                .setContentTitle("离线${Utils.getDownloadTitleByType(downloadType)}")
                // 内容文字
                .setContentText("已离线数目：$count")
                // 不会被滑动删除（常驻）
                .setOngoing(true)
                // 设置点击intent
        //设置通知栏的标题内容
        //创建通知
        val notification = builder.build()
        //设置为前台服务
        notificationManager?.notify(DOWNLOAD_DETAIL_NOTIFICATION, notification)
    }



    private fun getPartDetail(downloadType: Int, index: Int) {
        HttpManager.instance.getPartDetail(index * 500, 500, downloadType) {
            ScpDao.getInstance().insertDetailData(it)
            // 下载进度+1
            PreferenceUtil.addDetailDataLoadCount(downloadType)
            createNotification(downloadType, index*500+it.size)
            if (it.size > 499) {
                getPartDetail(downloadType, index+1)
            } else {
                // 标记下载完成
                PreferenceUtil.setDetailDataLoadFinish(downloadType, true)
                var allFinish = true
                // 判断列表里还有没有要下载的
                for (i in downloadList) {
                    // 按数字大小判断下一个要下载的
                    if (i > downloadType) {
                        getPartDetail(i, 0)
                        createNotification(i, 0)
                        allFinish = false
                        break
                    }
                }
                // 全都下载完成
                if (allFinish) {
                    notificationManager?.cancel(DOWNLOAD_DETAIL_NOTIFICATION)
                    notifyLoadFinish()
                }
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
