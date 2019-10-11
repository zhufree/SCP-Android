package info.free.scp.util

import java.util.*

object TimeUtil {
    val calendar = Calendar.getInstance()
    fun timeStampToStr(timeStamp: Long): String {
        calendar.timeInMillis = timeStamp
        return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}" +
                " ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)}"
    }
}