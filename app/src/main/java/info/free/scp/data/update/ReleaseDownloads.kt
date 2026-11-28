package info.free.scp.data.update

import java.io.EOFException
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.net.URI
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException
import javax.net.ssl.HttpsURLConnection

internal enum class ReleaseSource(val manifestUrl: URI) {
    R2(URI(R2_MANIFEST_URL)),
    GITHUB(URI(MANIFEST_URL));

    fun assetUrl(asset: UpdateFile): URI = when (this) {
        R2 -> r2ReleaseUrl(asset.releaseTag, asset.name)
        GITHUB -> asset.url
    }
}

/** Failures attributable to the remote response. Local reads/writes and policy checks propagate as-is. */
internal class ReleaseSourceFailure(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

internal class ReleaseSources(
    private val onAttempt: (ReleaseSource) -> Unit = {},
    private val onSuccess: (ReleaseSource) -> Unit = {}
) {
    private var preferred = ReleaseSource.R2

    fun <T> fetch(
        preflight: () -> Unit = {},
        onFallback: () -> Unit = {},
        request: (ReleaseSource) -> T
    ): T {
        preflight()
        try {
            return attempt(preferred, request)
        } catch (failure: ReleaseSourceFailure) {
            preflight()
            val alternate = if (preferred == ReleaseSource.R2) ReleaseSource.GITHUB else ReleaseSource.R2
            onFallback()
            try {
                return attempt(alternate, request).also { preferred = alternate }
            } catch (second: ReleaseSourceFailure) {
                preflight()
                second.addSuppressed(failure)
                throw second
            }
        }
    }

    private fun <T> attempt(source: ReleaseSource, request: (ReleaseSource) -> T): T {
        onAttempt(source)
        return request(source).also { onSuccess(source) }
    }

    fun fetchManifest(
        target: File,
        preflight: () -> Unit,
        download: (ReleaseSource, File) -> Unit = { source, file ->
            downloadReleaseFile(source.manifestUrl, file, MANIFEST_LIMIT, preflight = preflight)
        }
    ): UpdateManifest = fetch(preflight) { source ->
        download(source, target)
        val contents = target.readText(Charsets.UTF_8)
        try {
            parseManifest(contents)
        } catch (failure: IllegalArgumentException) {
            throw ReleaseSourceFailure(failure.message ?: "清单格式无效", failure)
        } catch (failure: IllegalStateException) {
            throw ReleaseSourceFailure(failure.message ?: "清单格式无效", failure)
        }
    }
}

private fun <T> readFromNetwork(action: () -> T): T = try {
    action()
} catch (failure: IOException) {
    throw ReleaseSourceFailure("数据下载连接失败", failure)
}

private fun downloadReleaseFile(
    initialUrl: URI,
    target: File,
    maxBytes: Long,
    requiredSize: Long? = null,
    preflight: () -> Unit,
    onBytes: (Long) -> Unit = {}
) {
    var targetUrl = initialUrl
    val manifest = requiredSize == null
    val r2 = initialUrl.host == ReleaseSource.R2.manifestUrl.host
    repeat(6) {
        preflight()
        val connection = readFromNetwork { targetUrl.toURL().openConnection() as HttpsURLConnection }
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = if (r2 || manifest) 4_000 else 6_000
            connection.readTimeout = if (manifest) 5_000 else 15_000
            connection.setRequestProperty("Accept-Encoding", "identity")
            if (manifest) connection.setRequestProperty("Cache-Control", "no-cache")
            val status = readFromNetwork { connection.responseCode }
            preflight()
            if (status in setOf(301, 302, 303, 307, 308)) {
                val location = connection.getHeaderField("Location")
                    ?: throw ReleaseSourceFailure("资源重定向地址缺失")
                val redirected = try {
                    targetUrl.resolve(location)
                } catch (failure: IllegalArgumentException) {
                    throw ReleaseSourceFailure("资源重定向地址无效", failure)
                }
                if (!allowedDownloadRedirect(targetUrl, redirected)) {
                    throw ReleaseSourceFailure("资源重定向地址不在允许列表中")
                }
                targetUrl = redirected
            } else {
                if (status != 200) throw ReleaseSourceFailure("下载失败（HTTP $status）")
                val advertisedSize = connection.getHeaderField("Content-Length")?.toLongOrNull()
                if (advertisedSize != null &&
                    (advertisedSize !in 1..maxBytes || requiredSize != null && advertisedSize != requiredSize)
                ) {
                    throw ReleaseSourceFailure("下载文件大小不匹配")
                }
                val input = readFromNetwork { connection.inputStream }
                val remote = object : FilterInputStream(input) {
                    override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
                        readFromNetwork { input.read(buffer, offset, length) }
                }
                val size = remote.use {
                    copyBounded(it, target, requiredSize ?: maxBytes, onBytes)
                }
                preflight()
                if (size <= 0 || requiredSize != null && size != requiredSize) {
                    throw ReleaseSourceFailure("下载文件大小不匹配")
                }
                return
            }
        } finally {
            connection.disconnect()
        }
    }
    throw ReleaseSourceFailure("资源重定向次数过多")
}

internal fun downloadVerifiedAsset(
    source: ReleaseSource,
    asset: UpdateFile,
    zipped: File,
    unpacked: File,
    preflight: () -> Unit,
    checkCancelled: () -> Unit,
    onBytes: (Long) -> Unit,
    onVerifying: () -> Unit,
    download: (URI, File, Long, () -> Unit, (Long) -> Unit) -> Unit = { url, file, size, before, progress ->
        downloadReleaseFile(url, file, size, size, before, progress)
    }
) {
    download(source.assetUrl(asset), zipped, asset.size, preflight, onBytes)
    checkCancelled()
    onVerifying()
    verifySha256(zipped, asset.sha256, checkCancelled)
    val size = try {
        zipped.inputStream().buffered().use { input ->
            GZIPInputStream(input).use { copyBounded(it, unpacked, asset.rawSize) { checkCancelled() } }
        }
    } catch (failure: ZipException) {
        throw ReleaseSourceFailure("数据文件无法解压", failure)
    } catch (failure: EOFException) {
        throw ReleaseSourceFailure("压缩文件不完整", failure)
    }
    if (size != asset.rawSize) throw ReleaseSourceFailure("解压后的文件大小不匹配")
    verifySha256(unpacked, asset.rawSha256, checkCancelled)
}
