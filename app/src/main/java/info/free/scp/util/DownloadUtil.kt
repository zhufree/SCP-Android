package info.free.scp.util

import com.lzy.okgo.OkGo
import com.lzy.okgo.request.GetRequest
import com.lzy.okserver.OkDownload
import okhttp3.OkHttpClient
import java.io.File


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
    fun downloadDb(url :String) {

    }
}