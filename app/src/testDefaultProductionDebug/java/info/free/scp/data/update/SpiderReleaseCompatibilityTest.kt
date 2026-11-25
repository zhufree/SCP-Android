package info.free.scp.data.update

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.zip.GZIPInputStream

class SpiderReleaseCompatibilityTest {
    @get:Rule val temp = TemporaryFolder()

    private val manifest by lazy {
        parseManifest(fixture("manifest.json").bufferedReader().use { it.readText() })
    }

    @Test fun matchesSpiderManifestIntegerToCatalogSqliteTextAndNumericSidecar() {
        val catalog = unpackAsset(manifest.catalog)
        connect(catalog).use { db ->
            assertEquals("text", db.scalar("SELECT typeof(value) FROM spider_meta WHERE key = 'exported_at'"))
            val installed = catalogVersion(
                listOf("export_id", "exported_at", "contract_version").associateWith { key ->
                    db.scalar("SELECT value FROM spider_meta WHERE key = '$key'")
                }
            )
            val sidecar = readAssetVersion("""
                {"export_id":"${installed.exportId}","exported_at":1795521600,"contract_version":1}
            """.trimIndent())
            assertTrue(sameCatalogExport(manifest.catalogVersion, installed))
            assertTrue(sameCatalogExport(installed, sidecar))
            assertTrue(sameCatalogExport(installed, installed.copy(exportedAt = "2026-11-24T12:00:00Z")))
        }
    }

    @Test fun manualImportRejectsSpiderRawPartAndAcceptsBothFullExportsAndLegacy() {
        val part = unpackAsset(manifest.parts.single { it.rows > 0 }.file)
        // It has readable content and would otherwise pass the manual import check.
        probe(part, requireContent = true)
        val failure = assertThrows(IllegalArgumentException::class.java) { probeFullImport(part) }
        assertTrue(failure.message.orEmpty().contains("我的 → 阅读设置 → 正文分块"))

        val full = unpack("full.db.gz")
        probeFullImport(full)
        probeFullImport(unpack("merged.db.gz"))
        connect(full).use { it.runSql("DROP TABLE spider_meta") }
        probeFullImport(full)
    }

    @Test fun zeroRowPartLeavesAReopenableLocalDatabaseAndCanBeFollowedByAnotherMerge() {
        val empty = manifest.parts.single { it.rows == 0L }
        val populated = manifest.parts.single { it.rows > 0 }
        val local = unpack("full.db.gz")
        connect(local).use { db ->
            db.runSql("DROP TABLE spider_meta")
            db.runSql("DELETE FROM scp_detail")
            db.runSql("PRAGMA user_version = 2")
            val sql = PartMergeSql("main")
            db.runSql(sql.createOwnership)
            db.runSql(sql.createState)
            db.runSql("INSERT INTO scp_detail (link, detail) VALUES ('/scp-2000', 'previous body')")
            db.runSql("INSERT INTO x_detail_part VALUES ('/scp-2000', ?)", empty.id)
            db.runSql(sql.insertState, empty.id, manifest.partsVersion, "old", "old", "old", 1, 0)
        }
        mergePart(local, empty)

        // Simulate cold-start recovery after an interrupted full-import promotion.
        // A promoted file with no readable content cannot replace the previous local database.
        val staging = temp.newFile("staging.db")
        val backup = File(local.path + ".bak")
        moveFile(local, backup)
        unpack("full.db.gz").copyTo(local)
        connect(local).use { it.runSql("DELETE FROM scp_detail") }
        val swap = DatabaseSwap(local, staging)
        assertTrue(swap.recover(::probeFullImport))
        assertFalse(backup.exists())
        assertThrows(IllegalArgumentException::class.java) { probeFullImport(local) }

        probe(local, requireContent = false)
        connect(local).use { reopened ->
            assertEquals("0", reopened.scalar("SELECT count(*) FROM scp_detail"))
            assertEquals("0", reopened.scalar("SELECT count(*) FROM x_detail_part"))
            assertEquals(empty.id, reopened.scalar("SELECT part_id FROM x_part_state WHERE rows = 0"))
        }

        mergePart(local, populated)
        probe(local, requireContent = false)
        connect(local).use { reopened ->
            assertEquals("<div>Synthetic body</div>", reopened.scalar("SELECT detail FROM scp_detail WHERE link = '/scp-001'"))
            assertEquals(populated.id, reopened.scalar("SELECT part_id FROM x_detail_part WHERE link = '/scp-001'"))
            assertEquals("2", reopened.scalar("SELECT count(*) FROM x_part_state"))
        }

        connect(local).use { it.runSql("DROP TABLE scp_detail") }
        assertThrows(SQLException::class.java) { probe(local, requireContent = false) }
    }

    private fun probeFullImport(file: File) {
        connect(file).use { db ->
            requireFullDetailSource(
                hasTable = { db.scalar("SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '$it')") == "1" },
                scalar = { db.scalar(it) }
            )
        }
        probe(file, requireContent = true)
    }

    private fun probe(file: File, requireContent: Boolean) =
        probeDetailDatabase(file, requireContent) { query ->
            connect(file).use { db ->
                db.createStatement().use { statement ->
                    statement.executeQuery(query).use { it.next() && it.getString(2).isNotBlank() }
                }
            }
        }

    private fun mergePart(local: File, part: DetailPart) {
        val file = unpackAsset(part.file)
        connect(file).use { source ->
            val sql = PartMergeSql("local")
            source.runSql("ATTACH DATABASE ? AS local", local.path)
            source.runSql("BEGIN IMMEDIATE")
            source.runSql(sql.createOwnership)
            source.runSql(sql.createState)
            source.runSql(sql.upsertFromPart)
            sql.removeRows("main.scp_detail").forEach { source.runSql(it, part.id) }
            source.runSql(sql.recordLinks("main.scp_detail"), part.id)
            source.runSql(sql.insertState, part.id, manifest.partsVersion, part.digest, part.exportId, part.file.sha256, part.rows, 0)
            source.runSql("COMMIT")
        }
    }

    private fun unpackAsset(asset: UpdateFile): File {
        val zipped = temp.newFile()
        fixture(asset.name).use { input -> assertEquals(asset.size, copyBounded(input, zipped, asset.size)) }
        verifySha256(zipped, asset.sha256)
        return temp.newFile().also { raw ->
            GZIPInputStream(zipped.inputStream().buffered()).use {
                assertEquals(asset.rawSize, copyBounded(it, raw, asset.rawSize))
            }
            verifySha256(raw, asset.rawSha256)
        }
    }

    private fun unpack(name: String): File = temp.newFile().also { file ->
        GZIPInputStream(fixture(name)).use { copyBounded(it, file, PART_LIMIT) }
    }

    private fun fixture(name: String) = checkNotNull(javaClass.getResourceAsStream("/spider-release/$name"))

    private fun connect(file: File): Connection = DriverManager.getConnection("jdbc:sqlite:$file")

    private fun Connection.scalar(sql: String): String = createStatement().use { statement ->
        statement.executeQuery(sql).use { rows ->
            check(rows.next())
            rows.getString(1)
        }
    }

    private fun Connection.runSql(sql: String, vararg args: Any) {
        prepareStatement(sql).use { statement ->
            args.forEachIndexed { index, value -> statement.setObject(index + 1, value) }
            statement.execute()
        }
    }
}
