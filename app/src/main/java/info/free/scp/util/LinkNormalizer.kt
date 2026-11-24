package info.free.scp.util

private val webLink = Regex("^(https?://)([^/]+)(.*)$", RegexOption.IGNORE_CASE)
private val fileScheme = Regex("^file://(?=/)", RegexOption.IGNORE_CASE)
private val percentEscape = Regex("%[0-9a-f]{2}")

/**
 * Returns the document key used by the user, catalog and detail databases.
 * Site URLs and local WebView URLs become relative paths. Other web URLs keep
 * their authority so that pages on different hosts keep distinct keys.
 * Null becomes an empty key. Escapes keep uppercase hex without being decoded.
 */
fun normalizeLink(link: String?): String {
    val clean = link.orEmpty().trim().substringBefore('#').substringBefore('?')
    val web = webLink.matchEntire(clean)
    val path = (web?.groupValues?.get(3)
        ?: clean.replaceFirst(fileScheme, ""))
        .trimEnd { it == '/' || it.isWhitespace() }.lowercase()
    val normalizedPath = percentEscape.replace(path) { it.value.uppercase() }
    if (web == null) return normalizedPath

    val authority = web.groupValues[2]
    val host = authority.substringAfterLast('@')
    if (authority.equals("scp-wiki-cn.wikidot.com", ignoreCase = true)) return normalizedPath
    return web.groupValues[1].lowercase() + authority.dropLast(host.length) + host.lowercase() + normalizedPath
}
