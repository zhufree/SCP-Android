package info.free.scp.util

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE
import android.database.Cursor
import android.net.Uri
import android.os.Environment.DIRECTORY_DOCUMENTS
import info.free.scp.ScpApplication


/**
 *
 * Created by zhufree on 2019/5/9.
 */

object DownloadUtil {
    object Status {
        const val NONE = 0 // LTGRAY
        const val FINISH = 1 // GREEN
        const val DOWNLOADING = 2 // DKBLUE
        const val PAUSE = 3 // LTBLUE
        const val NEED_UPDATE = 4 // YELLOW
        const val ERROR = 5 // RED

    }

    fun createDownload(dbUrl: String): Long {
        val request = DownloadManager.Request(Uri.parse(dbUrl))
        request.setDestinationInExternalPublicDir(DIRECTORY_DOCUMENTS, "scp_download/scp_data.db")
        // request.setTitle("TX QQ");
        // request.setDescription("This is TX QQ");
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(VISIBILITY_VISIBLE)
        return ScpApplication.downloadManager.enqueue(request)
    }

    fun getDownloadInfo(id: Long): IntArray {
        val uri = ScpApplication.downloadManager.getUriForDownloadedFile(id)
        val bytesAndStatus = intArrayOf(-1, -1, 0)
        val query = DownloadManager.Query().setFilterById(id)
        var c: Cursor? = null
        try {
            c = ScpApplication.downloadManager.query(query)
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            }
        } finally {
            c?.close()
        }
        return bytesAndStatus
    }
}