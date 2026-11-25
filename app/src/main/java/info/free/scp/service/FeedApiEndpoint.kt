package info.free.scp.service

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object FeedApiEndpoint {
    private const val BRIDGE_HOST = "api.zhufree.fun"
    private val defaultUrl = "https://$BRIDGE_HOST/".toHttpUrl()

    fun resolve(cachedUrl: String, buildUrl: String): HttpUrl {
        return normalize(cachedUrl) ?: normalize(buildUrl) ?: defaultUrl
    }

    private fun normalize(value: String): HttpUrl? {
        val url = value.trim().toHttpUrlOrNull() ?: return null
        val host = url.host.removeSuffix(".")
        if (host == "example.com" || host.endsWith(".example.com")) return null
        if (url.username.isNotEmpty() || url.password.isNotEmpty() ||
            url.query != null || url.fragment != null
        ) return null
        if (!url.isHttps && host != BRIDGE_HOST) return null

        return url.newBuilder().apply {
            scheme("https")
            if (!url.encodedPath.endsWith('/')) addPathSegment("")
        }.build()
    }
}
