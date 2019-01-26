package info.free.scp.util

import android.os.Environment
import android.text.SpannableStringBuilder
import android.util.Log
import java.io.File

object Logger {
    private val sp = File.separator
    private const val logDirName = "log"
    private const val appFolderName = "SCP"
    var local = false

    private var logString = SpannableStringBuilder()
    fun i(msg: String) {
        Log.i("freescp", msg)
        if (local) {
            logString.append("$msg\n")
        }
    }

    fun saveLog() {
        val logPath = "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp$logDirName"
        val logDir = File(logPath)
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        val logFile = File(logPath + sp + Utils.formatNow())
        logFile.createNewFile()
        logFile.writeText(logString.toString())
    }
}