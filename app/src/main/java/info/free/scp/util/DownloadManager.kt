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

object DownloadManager {
    fun downloadDb(url :String) {
        val builder = OkHttpClient.Builder()
        val request:GetRequest<File> = OkGo.get(url)
        val task = OkDownload.request(url, request).save()
        task.start()
    }
}