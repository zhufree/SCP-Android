package info.free.scp.data.update

import java.io.File
import java.net.URI
import javax.net.ssl.HttpsURLConnection

internal fun downloadReleaseFile(
    initialUrl: URI,
    target: File,
    maxBytes: Long,
    requiredSize: Long? = null,
    onBytes: (Long) -> Unit = {}
) {
    var targetUrl = initialUrl
    // Callers supply either the fixed manifest URL or releaseUrl(tag, name).
    require(initialUrl.toString() == MANIFEST_URL || isReleaseUrl(initialUrl)) { "发布地址无效" }
    repeat(6) {
        val connection = targetUrl.toURL().openConnection() as HttpsURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Accept-Encoding", "identity")
            val status = connection.responseCode
            if (status in setOf(301, 302, 303, 307, 308)) {
                val location = checkNotNull(connection.getHeaderField("Location")) { "资源重定向地址缺失" }
                val redirected = targetUrl.resolve(location)
                require(allowedDownloadRedirect(targetUrl, redirected)) { "资源重定向地址不在允许列表中" }
                targetUrl = redirected
            } else {
                check(status == 200) { "下载失败（HTTP $status）" }
                val advertisedSize = connection.getHeaderField("Content-Length")?.toLongOrNull()
                require(advertisedSize == null || advertisedSize in 1..maxBytes) { "下载文件超出大小限制" }
                val size = connection.inputStream.use {
                    copyBounded(it, target, minOf(maxBytes, requiredSize ?: maxBytes), onBytes)
                }
                require(size > 0 && (requiredSize == null || size == requiredSize)) { "下载文件大小不匹配" }
                return
            }
        } finally {
            connection.disconnect()
        }
    }
    error("资源重定向次数过多")
}
