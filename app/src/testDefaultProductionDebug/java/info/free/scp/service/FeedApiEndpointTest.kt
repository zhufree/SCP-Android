package info.free.scp.service

import org.junit.Assert.assertEquals
import org.junit.Test

class FeedApiEndpointTest {
    @Test
    fun resolvesCachedBuildAndPublicUrlsInOrder() {
        val publicUrl = "https://api.zhufree.fun/"
        val cases = listOf(
            Triple("", "", publicUrl),
            Triple(" ", "https://example.com/", publicUrl),
            Triple("https://EXAMPLE.COM./", "https://feed.test", "https://feed.test/"),
            Triple("broken", "https://example.com/", publicUrl),
            Triple("ftp://feed.test/", "https://build.test/", "https://build.test/"),
            Triple("http://feed.test/", "https://build.test/", "https://build.test/"),
            Triple("https://feed.test/?bad=1", "", publicUrl),
            Triple("http://api.zhufree.fun", "https://build.test/", publicUrl),
            Triple("", "http://api.zhufree.fun:80/", publicUrl),
            Triple(" https://feed.test:8443/custom ", "https://build.test/", "https://feed.test:8443/custom/"),
        )

        for ((cached, build, expected) in cases) {
            assertEquals("cached=$cached, build=$build", expected, FeedApiEndpoint.resolve(cached, build).toString())
        }
    }
}
