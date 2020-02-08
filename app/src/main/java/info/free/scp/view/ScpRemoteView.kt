package info.free.scp.view

import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import info.free.scp.R
import info.free.scp.ScpApplication
import info.free.scp.db.ScpDataHelper
import info.free.scp.view.detail.DetailActivity
import toast
import java.util.*
import java.util.Calendar.DAY_OF_WEEK

class ScpRemoteView(packageName: String) : RemoteViews(packageName, R.layout.layout_widget) {
    val today = Calendar.getInstance()

    fun updateToday() {
        setTextViewText(R.id.tv_widget_date, "${getWeekDayString(today.get(DAY_OF_WEEK))} " +
                "${today.get(Calendar.YEAR)}.${today.get(Calendar.MONTH) + 1}." +
                "${today.get(Calendar.DAY_OF_MONTH)}")
        val todayScp = ScpDataHelper.getInstance().getRandomScp()
        todayScp?.let {
            setTextViewText(R.id.tv_widget_scp_title, it.title)
            val intent = Intent(ScpApplication.context, DetailActivity::class.java)
            intent.putExtra("link", todayScp.link)
            val pendingIntent = PendingIntent.getActivity(ScpApplication.context, 5,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            setOnClickPendingIntent(R.id.tv_widget_scp_title, pendingIntent)
        }
    }

    private fun getWeekDayString(day: Int): String {
        return when (day) {
            1 -> "Sunday"
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> "Friday"
        }
    }
}