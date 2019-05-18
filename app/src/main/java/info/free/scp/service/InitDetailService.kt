package info.free.scp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import info.free.scp.util.PreferenceUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Progress
import com.lzy.okgo.request.GetRequest
import com.lzy.okserver.OkDownload
import com.lzy.okserver.download.DownloadListener
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.db.ScpDatabase
import info.free.scp.util.FileHelper
import java.io.File


class InitDetailService : IntentService("initDataService") {

    override fun onHandleIntent(intent: Intent?) {
        val link = PreferenceUtil.getDataDownloadLink(-1)
        val request: GetRequest<File> = OkGo.get(link)
        val task = OkDownload.request(link, request)
                .fileName("full_scp_data.db")
                .register(ServiceDownloadListener(this))
                .save()
        task.start()
    }

    internal class ServiceDownloadListener(var context: Context) : DownloadListener("download_full_db") {
        private val channelID = "info.free.scp"
        private val DOWNLOAD_DETAIL_NOTIFICATION = R.string.download_detail_service
        var notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        override fun onFinish(t: File?, progress: Progress?) {
            PreferenceUtil.setDetailDataLoadFinish(-1, true)
            FileHelper(context).copyDataBaseFile(t?.name?:"", true)
            ScpDatabase.getNewInstance()
            notificationManager.cancel(DOWNLOAD_DETAIL_NOTIFICATION)
        }

        override fun onRemove(progress: Progress?) {
            notificationManager.cancel(DOWNLOAD_DETAIL_NOTIFICATION)
        }

        override fun onProgress(progress: Progress?) {
            createNotification(progress)
        }

        override fun onError(progress: Progress?) {
            createNotification(progress)
        }

        override fun onStart(progress: Progress?) {
            createNotification(progress)
        }

        @RequiresApi(api = 26)
        private fun createNotifyChannel() {
            val channelName = "channel_name"
            val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }

        /**
         * Notification
         */
        private fun createNotification(progress: Progress?) {
            val builder = if (Build.VERSION.SDK_INT >= 26) {
                createNotifyChannel()
                Notification.Builder(context, channelID)
            } else Notification.Builder(context)
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    //设置通知栏横条的图标
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    // 立即启动
                    .setWhen(System.currentTimeMillis())
                    // 内容标题
                    .setContentTitle("离线文档数据库")
                    // 内容文字
                    .setContentText(getStatusNotice(progress?.status?:Progress.NONE) +
                            "：${progress?.currentSize?.div(1000)}KB/" +
                                "${progress?.totalSize?.div(1000000)}MB")
                    .setOngoing(true)
            // 不会被滑动删除（常驻）
            //创建通知
            val notification = builder.build()
            //设置为前台服务
            notificationManager.notify(DOWNLOAD_DETAIL_NOTIFICATION, notification)
        }

        private fun getStatusNotice(status: Int): String {
            return when (status) {
                Progress.NONE -> ""
                Progress.WAITING -> ""
                Progress.LOADING -> "离线中"
                Progress.PAUSE -> "离线暂停"
                Progress.ERROR -> "离线出错"
                Progress.FINISH -> "离线完成"
                else -> ""
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
