package info.free.scp.db

// Frozen v8 rules. Keep independent of the runtime link normalizer.
private val webLink = Regex("^(https?://)([^/]+)(.*)$", RegexOption.IGNORE_CASE)
private val fileScheme = Regex("^file://(?=/)", RegexOption.IGNORE_CASE)
private val percentEscape = Regex("%[0-9a-f]{2}")

internal fun normalizeLinkForV8(link: String?): String {
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

internal data class RecordV8(val title: String, val type: Int, val time: Long) {
    // One list state per link. Keep the latest action, with rowid order breaking ties.
    fun merge(next: RecordV8): RecordV8 = if (next.time >= time) next else this
}

internal data class LikeV8(
    val title: String,
    val liked: Boolean,
    val read: Boolean,
    val boxId: Int
) {
    // No timestamp or counters exist. Prefer an existing box on a liked row;
    // within the same priority, rows are processed from lowest to highest rowid.
    fun merge(next: LikeV8, boxIds: Set<Int>): LikeV8 {
        val previousBox = liked && boxId in boxIds
        val nextBox = next.liked && next.boxId in boxIds
        val preferred = when {
            previousBox != nextBox -> if (previousBox) this else next
            liked != next.liked -> if (liked) this else next
            else -> next
        }
        return preferred.copy(liked = liked || next.liked, read = read || next.read)
    }
}
