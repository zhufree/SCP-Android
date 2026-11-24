package info.free.scp.util

import info.free.scp.db.normalizeLinkForV8
import org.junit.Assert.assertEquals
import org.junit.Test

class LinkNormalizerTest {
    @Test
    fun normalizesLegacyArticleAndSiteUrls() {
        assertKey("/scp-cn-2901", "/SCP-CN-2901")
        assertKey("/scp-cn-2901", "HTTPS://SCP-WIKI-CN.WIKIDOT.COM/SCP-CN-2901/")
        assertKey("/scp-cn-2901", "file:///SCP-CN-2901")
    }

    @Test
    fun preservesEscapesAndDropsQueryFragmentAndTrailingSlash() {
        assertKey("/tag/%E7%BE%8E", "/TAG/%e7%bE%8e///?view=1#pages")
        assertKey("/system:page-tags/tag/eric", "/system:page-tags/tag/eric#pages")
        assertKey("/page%2F", "/PAGE%2f/")
    }

    @Test
    fun retainsExternalHostAndNormalizesCase() {
        assertKey("https://other.example/page", "HTTPS://Other.Example/Page/?q=1")
    }

    @Test
    fun handlesEmptyInputs() {
        assertKey("", null)
        assertKey("", "")
        assertKey("", "  ")
        assertKey("", "/")
    }

    private fun assertKey(expected: String, input: String?) {
        assertEquals(expected, normalizeLink(input))
        assertEquals(expected, normalizeLink(expected))
        assertEquals(expected, normalizeLinkForV8(input))
        assertEquals(expected, normalizeLinkForV8(expected))
    }
}
