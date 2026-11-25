package info.free.scp.data.update

import java.io.File

internal fun requireNonEmpty(file: File) {
    require(file.isFile && file.length() > 0) { "${file.name} 缺失或为空" }
}

internal fun validateDatabase(
    file: File,
    maxVersion: Int,
    requiredTables: (Int) -> List<String>,
    scalar: (String) -> String,
    hasTable: (String) -> Boolean
) {
    requireNonEmpty(file)
    val version = scalar("PRAGMA user_version").toInt()
    require(version in 0..maxVersion) { "数据库版本 $version 高于当前支持的版本 $maxVersion" }
    require(scalar("PRAGMA quick_check") == "ok") { "数据库完整性检查失败" }
    requiredTables(version).forEach { table ->
        require(hasTable(table)) { "数据文件缺少 $table 表" }
    }
}

private const val VALID_DETAIL_PROBE = """
    SELECT link, detail, not_found, tags FROM scp_detail
    WHERE typeof(link) = 'text' AND length(trim(link)) > 0
        AND typeof(detail) = 'text' AND length(trim(detail)) > 0 AND trim(detail) != 'null'
        AND (not_found IS NULL OR (typeof(not_found) = 'integer' AND not_found != 1 AND not_found != 404))
        AND typeof(tags) IN ('null', 'text')
    LIMIT 1
"""

internal fun requireFullDetailSource(hasTable: (String) -> Boolean, scalar: (String) -> String) {
    require(!hasTable("spider_meta") ||
        scalar("SELECT EXISTS(SELECT 1 FROM spider_meta WHERE key = 'part_id')") == "0") {
        "这是正文分块，请前往我的 → 阅读设置 → 正文分块安装"
    }
    require(!hasTable("x_part_state") && !hasTable("x_detail_part")) { "请选择整库发布文件" }
}

internal fun probeDetailDatabase(file: File, requireContent: Boolean, query: (String) -> Boolean) {
    requireNonEmpty(file)
    if (requireContent) {
        require(query(VALID_DETAIL_PROBE)) { "正文库没有可读取的正文，请选择非空的整库发布文件" }
    } else {
        // A merged snapshot can be empty. Opening Room and querying its schema is enough.
        query("SELECT link, detail, not_found, tags FROM scp_detail LIMIT 0")
    }
}
