package info.free.scp.data.update

import kotlinx.coroutines.CancellationException
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
    private val manifestV2 by lazy {
        parseManifest(fixture("manifest-v2.json").bufferedReader().use { it.readText() })
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

    @Test fun validatesFivePartV2ManifestAndAssets() {
        assertEquals("2", manifestV2.partsVersion)
        assertEquals(
            listOf("scp-en", "tales-cn", "scp-cn", "creative", "library"),
            manifestV2.parts.map { it.id }
        )
        manifestV2.parts.forEach { part ->
            connect(unpackAsset(part.file)).use { db ->
                assertEquals("0", db.scalar("PRAGMA user_version"))
                assertEquals("ok", db.scalar("PRAGMA quick_check"))
                val meta = db.createStatement().use { statement ->
                    statement.executeQuery("SELECT key, value FROM spider_meta").use { rows ->
                        buildMap { while (rows.next()) put(rows.getString(1), rows.getString(2)) }
                    }
                }
                assertEquals(part.label, meta["label"])
                requirePartMetadata(meta, manifestV2.partsVersion, part) {
                    db.scalar("SELECT count(*) FROM scp_detail").toLong()
                }
                assertThrows(IllegalArgumentException::class.java) {
                    requirePartMetadata(meta, manifest.partsVersion, part) { part.rows }
                }
            }
        }
    }

    @Test fun upgradesOldPartsAndRetriesOnlyTheBlockWhoseMergeFailed() {
        val local = unpack("full.db.gz")
        manifest.parts.forEach { mergePart(local, it) }
        val replacement = manifestV2.parts[1]
        connect(local).use { db ->
            db.runSql("INSERT INTO scp_detail (link, detail) VALUES ('/old-tale', 'old story')")
            db.runSql("INSERT INTO x_detail_part VALUES ('/old-tale', ?)", replacement.id)
            db.runSql(PartMergeSql("main").insertState, replacement.id, "1", replacement.digest, "old", "old", 1, 0)
            db.runSql("""
                CREATE TRIGGER reject_tales BEFORE INSERT ON x_part_state
                WHEN NEW.part_id = 'tales-cn'
                BEGIN SELECT RAISE(ABORT, 'simulated merge failure'); END
            """.trimIndent())
        }

        fun pending() = pendingPartDownloads(
            manifestV2.parts, planParts(manifestV2.partsVersion, manifestV2.parts, inventory(local))
        )

        val attempts = mutableListOf<Pair<Int, String>>()
        val failures = installDetailParts(pending()) { index, part ->
            attempts += index to part.id
            mergePart(local, part, manifestV2)
        }
        assertEquals(manifestV2.parts.mapIndexed { index, part -> (index + 1) to part.id }, attempts)
        assertEquals(listOf(replacement), failures.map { it.part })
        assertTrue(failures.single().cause is SQLException)
        val partiallyInstalled = inventory(local)
        assertEquals(manifestV2.parts.map { it.id }.toSet(), partiallyInstalled.keys)
        assertEquals("1", partiallyInstalled.getValue(replacement.id).version)
        connect(local).use { db ->
            assertEquals("0", db.scalar("SELECT count(*) FROM x_detail_part WHERE part_id LIKE 'scp-en-%'"))
            assertEquals("scp-en", db.scalar("SELECT part_id FROM x_detail_part WHERE link = '/scp-001'"))
            assertEquals("old story", db.scalar("SELECT detail FROM scp_detail WHERE link = '/old-tale'"))
            assertEquals("0", db.scalar("SELECT count(*) FROM scp_detail WHERE link = '/synthetic-cn-tale'"))
            db.runSql("DROP TRIGGER reject_tales")
        }

        attempts.clear()
        assertTrue(installDetailParts(pending()) { index, part ->
            attempts += index to part.id
            mergePart(local, part, manifestV2)
        }.isEmpty())
        assertEquals(listOf(1 to replacement.id), attempts)
        assertTrue(pending().isEmpty())
        assertTrue(inventory(local).values.all { it.version == "2" })
        connect(local).use { db ->
            assertEquals("0", db.scalar("SELECT count(*) FROM scp_detail WHERE link = '/old-tale'"))
            assertEquals("4", db.scalar("SELECT count(*) FROM scp_detail"))
        }
    }

    @Test fun batchSkipsLatestAndOversizedPartsAndStopsOnCancellation() {
        val (latest, oversized, first, cancelled, remaining) = manifestV2.parts
        val parts = listOf(latest, oversized.copy(file = oversized.file.copy(rawSize = PART_LIMIT + 1)), first, cancelled, remaining)
        val installed = mutableMapOf(latest.id to InstalledPart("2", latest.digest))
        fun pending() = pendingPartDownloads(parts, planParts("2", parts, installed))

        val expected = listOf(first, cancelled, remaining)
        assertEquals(expected, pending())
        assertThrows(CancellationException::class.java) {
            installDetailParts(pending()) { _, part ->
                if (part == cancelled) throw CancellationException()
                installed[part.id] = InstalledPart("2", part.digest)
            }
        }
        assertEquals(listOf(cancelled, remaining), pending())
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

    private fun inventory(local: File): Map<String, InstalledPart> = connect(local).use { db ->
        if (db.scalar("SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE name = 'x_part_state')") == "0") {
            return@use emptyMap()
        }
        db.createStatement().use { statement ->
            statement.executeQuery("SELECT part_id, parts_version, digest FROM x_part_state").use { rows ->
                buildMap { while (rows.next()) put(rows.getString(1), InstalledPart(rows.getString(2), rows.getString(3))) }
            }
        }
    }

    private fun mergePart(local: File, part: DetailPart, manifest: UpdateManifest = this.manifest) {
        val retired = planParts(manifest.partsVersion, manifest.parts, inventory(local))
            .filterValues { it == PartAction.DELETE }.keys
        val file = unpackAsset(part.file)
        connect(file).use { source ->
            val sql = PartMergeSql("local")
            source.runSql("ATTACH DATABASE ? AS local", local.path)
            source.runSql("BEGIN IMMEDIATE")
            source.runSql(sql.createOwnership)
            source.runSql(sql.createState)
            retired.forEach { id ->
                sql.removeRows().forEach { source.runSql(it, id) }
                source.runSql(sql.deleteState, id)
            }
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
