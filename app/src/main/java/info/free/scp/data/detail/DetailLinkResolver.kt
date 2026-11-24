package info.free.scp.data.detail

import info.free.scp.SCPConstants.SCP_SITE_URL
import info.free.scp.util.normalizeLink

object DetailLinkResolver {
    fun toFullUrl(link: String): String {
        val target = link.replaceFirst(fileScheme, "")
        return if (absoluteLink.containsMatchIn(target)) target else "$SCP_SITE_URL$target"
    }

    fun toLocalLink(requestUrl: String): String = normalizeLink(requestUrl)

    fun toApiPath(link: String): String = normalizeLink(link).removePrefix("/")

    private val fileScheme = Regex("^file://(?=/)", RegexOption.IGNORE_CASE)
    private val absoluteLink = Regex("^(https?://|//)", RegexOption.IGNORE_CASE)
}
