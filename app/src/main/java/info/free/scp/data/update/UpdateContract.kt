package info.free.scp.data.update

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

internal const val MIB = 1024L * 1024
internal const val MANIFEST_LIMIT = 64L * 1024
internal const val CATEGORY_LIMIT = 50 * MIB
internal const val PART_LIMIT = 200 * MIB
internal const val CHECK_INTERVAL = 24L * 60 * 60 * 1000
internal const val MANIFEST_URL = "https://github.com/zhufree/SCP-Spider/releases/latest/download/manifest.json"
private const val RELEASE_BASE = "https://github.com/zhufree/SCP-Spider/releases/download/"
private val segment = Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,199}")
private val digestPattern = Regex("[0-9a-f]{64}")
// 待确认：首次公开 Release 时核对 GitHub 实际返回的资源域名。
internal val GITHUB_RESOURCE_HOSTS = setOf("release-assets.githubusercontent.com", "objects.githubusercontent.com")

internal fun releaseUrl(tag: String, name: String): URI {
    require(segment.matches(tag) && segment.matches(name)) { "发布标签或文件名无效" }
    return URI(RELEASE_BASE + tag + "/" + name)
}

internal fun isReleaseUrl(uri: URI): Boolean {
    val value = uri.toString()
    if (!value.startsWith(RELEASE_BASE)) return false
    val path = value.removePrefix(RELEASE_BASE).split('/')
    return path.size == 2 && path.all(segment::matches)
}

internal fun allowedRedirect(target: URI, hosts: Set<String> = GITHUB_RESOURCE_HOSTS): Boolean =
    target.scheme.equals("https", ignoreCase = true) &&
        target.host?.lowercase(Locale.ROOT) in hosts && target.rawUserInfo == null &&
        (target.port == -1 || target.port == 443) && target.rawFragment == null

internal fun allowedDownloadRedirect(from: URI, to: URI): Boolean =
    allowedRedirect(to) ||
        // GitHub may resolve latest to a tagged download on its release origin before the CDN hop.
        (from.toString() == MANIFEST_URL && isReleaseUrl(to) && to.path.endsWith("/manifest.json"))

internal data class CatalogVersion(val exportId: String, val exportedAt: String, val contractVersion: String) {
    companion object {
        val Legacy = CatalogVersion("legacy", "0001-01-01T00:00:00Z", "0")
    }
}

internal fun requireSupportedContract(version: String) {
    require(version == "1") { "数据契约版本 $version 暂不支持，请升级 App" }
}

internal fun catalogVersion(values: Map<String, String>, requireSupported: Boolean = true): CatalogVersion {
    val version = CatalogVersion(
        values["export_id"].orEmpty(), values["exported_at"].orEmpty(), values["contract_version"].orEmpty()
    )
    require(version.exportId.isNotBlank()) { "缺少 export_id" }
    require(version.contractVersion.isNotBlank()) { "缺少 contract_version" }
    if (requireSupported) requireSupportedContract(version.contractVersion)
    timestamp(version.exportedAt)
    return version
}

internal fun readAssetVersion(json: String): CatalogVersion {
    val obj = Json.parseToJsonElement(json) as JsonObject
    val values = listOf("export_id", "exported_at", "contract_version").associateWith(obj::field)
    if (values == mapOf("export_id" to "legacy", "exported_at" to CatalogVersion.Legacy.exportedAt, "contract_version" to "0")) {
        return CatalogVersion.Legacy
    }
    return catalogVersion(values)
}

internal fun newerCatalog(candidate: CatalogVersion, current: CatalogVersion?): Boolean {
    if (candidate == CatalogVersion.Legacy) return current == null
    requireSupportedContract(candidate.contractVersion)
    val candidateTime = timestamp(candidate.exportedAt)
    if (current == null || current == CatalogVersion.Legacy) return true
    if (candidate.exportId == current.exportId && candidate.contractVersion == current.contractVersion) return false
    val currentTime = timestamp(current.exportedAt)
    return candidateTime.first > currentTime.first ||
        (candidateTime.first == currentTime.first && candidateTime.second > currentTime.second)
}

internal fun sameCatalogExport(a: CatalogVersion, b: CatalogVersion): Boolean =
    a.exportId == b.exportId && a.contractVersion == b.contractVersion &&
        timestamp(a.exportedAt) == timestamp(b.exportedAt)

private val unixSeconds = Regex("-?[0-9]+")
private val isoTimestamp = Regex(
    """(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d{1,9}))?(Z|[+-]\d{2}:\d{2})"""
)

private fun timestamp(value: String): Pair<Long, Int> {
    if (unixSeconds.matches(value)) {
        val seconds = requireNotNull(value.toLongOrNull()) { "exported_at 超出范围" }
        return seconds to 0
    }
    val match = requireNotNull(isoTimestamp.matchEntire(value)) { "exported_at 格式无效" }
    val (seconds, fraction, zone) = match.destructured
    val offset = if (zone == "Z") "+0000" else zone.replace(":", "")
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT).apply {
        calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ROOT).apply {
            gregorianChange = Date(Long.MIN_VALUE)
        }
        isLenient = false
    }
    val date = requireNotNull(parser.parse(seconds + offset, ParsePosition(0))) { "exported_at 格式无效" }
    return date.time / 1000 to fraction.padEnd(9, '0').toInt()
}

internal data class UpdateFile(
    val name: String, val releaseTag: String, val size: Long, val sha256: String,
    val rawSize: Long, val rawSha256: String
) {
    val url: URI get() = releaseUrl(releaseTag, name)
}

internal data class DetailPart(
    val id: String, val label: String, val rows: Long, val digest: String,
    val exportId: String, val file: UpdateFile
) {
    val exceedsSizeLimit: Boolean get() = file.size > PART_LIMIT || file.rawSize > PART_LIMIT
}

internal data class UpdateManifest(
    val catalogVersion: CatalogVersion, val catalog: UpdateFile,
    val partsVersion: String, val parts: List<DetailPart>
)

internal fun requirePartMetadata(
    meta: Map<String, String>, partsVersion: String, part: DetailPart, countRows: () -> Long
) {
    requireSupportedContract(meta["contract_version"].orEmpty())
    require(meta["export_id"] == part.exportId && meta["part_id"] == part.id &&
        meta["parts_version"] == partsVersion && meta["content_digest"] == part.digest &&
        meta["rows"]?.toLongOrNull() == part.rows && countRows() == part.rows
    ) { "正文块 spider_meta 或行数与清单不一致" }
}

private fun JsonObject.field(key: String): String {
    val value = this[key] as? JsonPrimitive
    require(value != null && value.content != "null" && value.content.isNotBlank()) { "清单缺少 $key" }
    return value.content
}

private fun JsonObject.hash(key: String): String = field(key).also {
    require(digestPattern.matches(it)) { "$key 无效" }
}

private fun JsonObject.number(key: String, range: LongRange): Long {
    val number = field(key).toLongOrNull()
    require(number != null && number in range) { "$key 超出范围" }
    return number
}

internal fun parseManifest(json: String): UpdateManifest {
    val root = Json.parseToJsonElement(json) as? JsonObject ?: error("清单格式无效")
    require(root.field("manifest_version") == "1") { "清单版本暂不支持，请升级 App" }
    val tag = root.field("tag")
    val version = catalogVersion(
        listOf("export_id", "exported_at", "contract_version").associateWith(root::field)
    )
    val partsVersion = root.number("parts_version", 1..Int.MAX_VALUE.toLong()).toString()
    val assets = root["assets"] as? JsonArray ?: error("清单缺少 assets")
    var catalog: UpdateFile? = null
    val parts = mutableListOf<DetailPart>()
    val usedFiles = mutableSetOf<URI>()
    val usedPartIds = mutableSetOf<String>()
    for (asset in assets) {
        val item = asset as? JsonObject ?: error("清单资产格式无效")
        val kind = item.field("kind")
        require(kind == "category" || kind == "part") { "未知资产类型：$kind" }
        // Keep all declared part IDs so a size-limited part cannot look retired.
        val limit = if (kind == "category") CATEGORY_LIMIT else Long.MAX_VALUE
        val file = UpdateFile(
            item.field("name"), item.field("release_tag"), item.number("size", 1..limit),
            item.hash("sha256"), item.number("raw_size", 1..limit), item.hash("raw_sha256")
        )
        require(usedFiles.add(file.url) && file.name.endsWith(".db.gz")) { "清单资产名无效或重复" }
        if (kind == "category") {
            require(catalog == null && file.releaseTag == tag) { "目录库发布标签不匹配或重复" }
            catalog = file
        } else {
            require(file.releaseTag == tag) { "正文块发布标签不匹配" }
            val id = item.field("part_id")
            require(segment.matches(id) && usedPartIds.add(id)) { "正文块编号无效或重复" }
            parts += DetailPart(
                id, item.field("label"), item.number("rows", 0..Int.MAX_VALUE.toLong()),
                item.hash("content_digest"), item.field("part_export_id"), file
            )
        }
    }
    return UpdateManifest(version, requireNotNull(catalog) { "清单缺少目录库" }, partsVersion, parts)
}

internal fun sha256(file: File, checkCancelled: () -> Unit = {}): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().buffered().use { input ->
        val buffer = ByteArray(64 * 1024)
        while (true) {
            checkCancelled()
            val count = input.read(buffer)
            if (count == -1) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(Locale.ROOT, it.toInt() and 0xff) }
}

internal fun verifySha256(file: File, expected: String, checkCancelled: () -> Unit = {}) {
    require(digestPattern.matches(expected) && sha256(file, checkCancelled) == expected) { "数据文件 SHA-256 不匹配" }
}

internal fun enoughPartSpace(rawSize: Long, availableBytes: Long): Boolean =
    rawSize in 1..PART_LIMIT && availableBytes >= rawSize * 3 + 50 * MIB

internal fun readCachedManifest(file: File): UpdateManifest? =
    if (file.isFile && file.length() in 1..MANIFEST_LIMIT) {
        runCatching { parseManifest(file.readText(Charsets.UTF_8)) }.getOrNull()
    } else null

internal fun checkDue(lastCheck: Long, now: Long): Boolean =
    lastCheck == 0L || (now >= lastCheck && now - lastCheck >= CHECK_INTERVAL)

internal fun shouldCheckManifest(cacheFile: File, cached: UpdateManifest?, lastCheck: Long, now: Long): Boolean =
    (cached == null && cacheFile.exists()) || checkDue(lastCheck, now)

internal data class RoomColumn(val type: String, val notNull: Boolean = false, val primaryKey: Int = 0)

internal val CATALOG_COLUMNS = mapOf(
    "_id" to RoomColumn("INTEGER", true, 1), "_index" to RoomColumn("INTEGER", true),
    "title" to RoomColumn("TEXT", true), "link" to RoomColumn("TEXT", true),
    "scp_type" to RoomColumn("INTEGER", true), "author" to RoomColumn("TEXT"),
    "created_time" to RoomColumn("TEXT"), "sub_scp_type" to RoomColumn("TEXT")
)

internal val DETAIL_COLUMNS = mapOf(
    "link" to RoomColumn("TEXT", true, 1), "detail" to RoomColumn("TEXT"),
    "not_found" to RoomColumn("INTEGER"), "tags" to RoomColumn("TEXT")
)

internal fun matchesRoomColumns(expected: Map<String, RoomColumn>, actual: Map<String, RoomColumn>) =
    expected == actual

internal data class InstalledPart(val version: String, val digest: String)
internal enum class PartAction { INSTALL, REPLACE, DELETE, SKIP, TOO_LARGE }

internal fun planParts(
    version: String, assets: List<DetailPart>, installed: Map<String, InstalledPart>
): Map<String, PartAction> = buildMap {
    for (part in assets) {
        val existing = installed[part.id]
        put(part.id, when {
            existing?.version == version && existing.digest == part.digest -> PartAction.SKIP
            part.exceedsSizeLimit -> PartAction.TOO_LARGE
            existing == null -> PartAction.INSTALL
            else -> PartAction.REPLACE
        })
    }
    for ((id, existing) in installed) {
        if (id !in this && existing.version != version) put(id, PartAction.DELETE)
    }
}
