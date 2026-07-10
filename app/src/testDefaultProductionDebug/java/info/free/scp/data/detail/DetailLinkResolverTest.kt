package info.free.scp.data.detail

import info.free.scp.SCPConstants.SCP_SITE_URL
import org.junit.Assert.assertEquals
import org.junit.Test

class DetailLinkResolverTest {
    @Test
    fun fullUrlPrefixesLocalDocumentLink() {
        assertEquals(
            "$SCP_SITE_URL/scp-173",
            DetailLinkResolver.toFullUrl("/scp-173")
        )
    }

    @Test
    fun fullUrlKeepsAbsoluteLink() {
        val link = "https://scp-wiki-cn.wikidot.com/scp-173"

        assertEquals(link, DetailLinkResolver.toFullUrl(link))
    }

    @Test
    fun localLinkRemovesSitePrefix() {
        assertEquals(
            "/scp-173",
            DetailLinkResolver.toLocalLink("$SCP_SITE_URL/scp-173")
        )
    }

    @Test
    fun localLinkRemovesFileSchemePrefix() {
        assertEquals(
            "/scp-173",
            DetailLinkResolver.toLocalLink("file:///scp-173")
        )
    }

    @Test
    fun apiPathRemovesLeadingSlash() {
        assertEquals("scp-173", DetailLinkResolver.toApiPath("/scp-173"))
    }
}
