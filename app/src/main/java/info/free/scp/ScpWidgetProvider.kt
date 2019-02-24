package info.free.scp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.ScpRemoteView

class ScpWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray) {
//        val pendingIntent = PendingIntent.getBroadcast(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        remoteViews.setOnClickPendingIntent(R.id.ll_widget_container, pendingIntent)

        for (appWidgetId in appWidgetIds) {
            val remoteViews = ScpRemoteView(context.packageName)
//            if (PreferenceUtil.checkNeedUpdateWidget()) {
                remoteViews.updateToday()
                PreferenceUtil.setLastUpdateWidgetTime(System.currentTimeMillis())
//            }
//            val intent = Intent(context, MainActivity::class.java)
//            val pendingIntent = PendingIntent.getActivity(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            remoteViews.setOnClickPendingIntent(R.id.ll_widget_container, pendingIntent)
            appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}