package info.free.scp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import info.free.scp.util.EventUtil
import info.free.scp.util.PreferenceUtil
import info.free.scp.view.ScpRemoteView

class ScpWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray) {

        for (appWidgetId in appWidgetIds) {
            val remoteViews = ScpRemoteView(context.packageName)
            remoteViews.updateToday()
            appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        EventUtil.onEvent(context, EventUtil.addWidget)
    }
}