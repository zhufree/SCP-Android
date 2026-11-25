package info.free.scp.service

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FeedApiServiceCacheTest {
    @Test
    fun nextHomeRequestUsesUpdatedAddressAndSameClient() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            requestedUrls.add(request.url.toString())
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"latest_create":[],"latest_translate":[]}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()

        var cachedUrl = ""
        val services = FeedApiServiceCache(client) { FeedApiEndpoint.resolve(cachedUrl, "https://example.com/") }
        val initialService = services.get()
        initialService.getLatestIndex()

        cachedUrl = "https://feed.test:8443"
        val updatedService = services.get()
        updatedService.getLatestIndex()
        assertSame(updatedService, services.get())

        assertEquals(
            listOf("https://api.zhufree.fun/latest_index/", "https://feed.test:8443/latest_index/"),
            requestedUrls,
        )
    }
}
