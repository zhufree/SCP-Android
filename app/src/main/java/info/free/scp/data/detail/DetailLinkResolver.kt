package info.free.scp.data.detail

import info.free.scp.SCPConstants.SCP_SITE_URL

object DetailLinkResolver {
    fun toFullUrl(link: String): String {
        return if (link.contains("http")) link else "$SCP_SITE_URL$link"
    }

    fun toLocalLink(requestUrl: String): String {
        return when {
            requestUrl.startsWith(SCP_SITE_URL) -> requestUrl.removePrefix(SCP_SITE_URL)
            requestUrl.startsWith("file:") -> requestUrl.removePrefix("file://")
            else -> requestUrl
        }
    }

    fun toApiPath(link: String): String {
        return link.substring(1)
    }
}
