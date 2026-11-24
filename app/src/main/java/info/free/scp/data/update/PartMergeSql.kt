package info.free.scp.data.update

/** Shared by the Android importer and the SQLite JDBC fixture tests. */
internal class PartMergeSql(private val target: String) {
    private val details = "$target.scp_detail"
    private val ownership = "$target.x_detail_part"
    private val state = "$target.x_part_state"
    private val columns = "link, detail, not_found, tags"

    val createOwnership = """
        CREATE TABLE IF NOT EXISTS $ownership (
            link TEXT NOT NULL PRIMARY KEY, part_id TEXT NOT NULL)
    """.trimIndent()
    val createState = """
        CREATE TABLE IF NOT EXISTS $state (
            part_id TEXT NOT NULL PRIMARY KEY, parts_version TEXT NOT NULL, digest TEXT NOT NULL,
            export_id TEXT NOT NULL, sha256 TEXT NOT NULL, rows INTEGER NOT NULL, installed_at INTEGER NOT NULL)
    """.trimIndent()

    val upsertFromPart = "INSERT OR REPLACE INTO $details ($columns) SELECT $columns FROM main.scp_detail"
    val upsertRow = "INSERT OR REPLACE INTO $details ($columns) VALUES (?, ?, ?, ?)"
    fun recordLinks(snapshot: String) = """
        INSERT OR REPLACE INTO $ownership (link, part_id) SELECT link, ? FROM $snapshot
    """.trimIndent()
    val insertState = """
        INSERT OR REPLACE INTO $state (part_id, parts_version, digest, export_id, sha256, rows, installed_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

    fun removeRows(keepLinks: String? = null): List<String> {
        val missing = keepLinks?.let { " AND NOT EXISTS (SELECT 1 FROM $it AS snapshot WHERE snapshot.link = $ownership.link)" }
            .orEmpty()
        return listOf(
            "DELETE FROM $details WHERE link IN (SELECT link FROM $ownership WHERE part_id = ?$missing)",
            "DELETE FROM $ownership WHERE part_id = ?$missing"
        )
    }

    val deleteState = "DELETE FROM $state WHERE part_id = ?"
}
