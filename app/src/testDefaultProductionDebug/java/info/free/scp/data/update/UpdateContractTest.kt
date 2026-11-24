package info.free.scp.data.update

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.URI

class UpdateContractTest {
    @get:Rule val temp = TemporaryFolder()
    private val hash = "a".repeat(64)

    @Test fun parsesManifestAndBuildsUrlsWithoutUsingProvidedUrl() {
        val parsed = parseManifest(manifest())
        assertEquals("new-export", parsed.catalogVersion.exportId)
        assertEquals("7", parsed.partsVersion)
        assertEquals(releaseUrl("db-20261124-1", "category.db.gz"), parsed.catalog.url)
        val part = parsed.parts.single()
        assertEquals("reused-export", part.exportId)
        assertEquals(3L, part.rows)
        assertEquals(releaseUrl("detail-parts", "detail-misc-aabb.db.gz"), part.file.url)
    }

    @Test fun rejectsMissingRequiredFieldsAndUnknownVersions() {
        assertThrows(IllegalArgumentException::class.java) {
            parseManifest(manifest().replace("\"contract_version\": 1,", ""))
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseManifest(manifest().replace("\"contract_version\": 1", "\"contract_version\": 2"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseManifest(manifest().replace("\"manifest_version\": 1", "\"manifest_version\": 2"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseManifest(manifest().replace(",\"part_export_id\":\"reused-export\"", ""))
        }
    }

    @Test fun rejectsConflictingTagsAndOversizedAssets() {
        assertThrows(IllegalArgumentException::class.java) {
            parseManifest(manifest().replace("\"release_tag\":\"detail-parts\"", "\"release_tag\":\"work-state\""))
        }
        assertThrows(IllegalArgumentException::class.java) {
            parseManifest(manifest().replace("\"raw_size\":21", "\"raw_size\":${PART_LIMIT + 1}"))
        }
    }

    @Test fun comparesCatalogIdentityTimesAndLegacyPriority() {
        val current = CatalogVersion("installed", "2026-11-24T12:00:00Z", "1")
        assertTrue(newerCatalog(current, CatalogVersion.Legacy))
        assertTrue(newerCatalog(CatalogVersion.Legacy, null))
        assertFalse(newerCatalog(CatalogVersion.Legacy, current))
        assertFalse(newerCatalog(CatalogVersion.Legacy, CatalogVersion.Legacy))
        assertFalse(newerCatalog(current.copy(exportedAt = "2026-11-25T00:00:00Z"), current))
        assertFalse(newerCatalog(current.copy(exportId = "older", exportedAt = "2026-11-24T12:01:00+01:00"), current))
        val equivalentTime = current.copy(exportId = "same-time", exportedAt = "2026-11-24T20:00:00.000+08:00")
        assertTrue(sameExportTime(equivalentTime, current))
        assertFalse(newerCatalog(equivalentTime, current))
        assertTrue(newerCatalog(equivalentTime.copy(exportedAt = "2026-11-24T12:00:00.000000001Z"), current))
        assertThrows(IllegalArgumentException::class.java) {
            newerCatalog(current.copy(contractVersion = "2"), CatalogVersion.Legacy)
        }
    }

    @Test fun recognizesBundledLegacyAndValidatesModernMetadata() {
        val bundled = readAssetVersion("""
            {"export_id":"legacy","exported_at":"0001-01-01T00:00:00Z","contract_version":"0"}
        """.trimIndent())
        assertEquals(CatalogVersion.Legacy, bundled)
        assertTrue(newerCatalog(parseManifest(manifest()).catalogVersion, bundled))
        assertThrows(IllegalArgumentException::class.java) {
            readAssetVersion("""{"export_id":"one","exported_at":"2026-02-31T00:00:00Z","contract_version":"1"}""")
        }
    }

    @Test fun enforcesReleaseAndRedirectAllowlists() {
        val latest = URI(MANIFEST_URL)
        val canonicalManifest = releaseUrl("db-20261124-1", "manifest.json")
        assertTrue(isReleaseUrl(canonicalManifest))
        assertTrue(allowedDownloadRedirect(latest, canonicalManifest))
        assertFalse(allowedDownloadRedirect(canonicalManifest, releaseUrl("other", "manifest.json")))
        assertFalse(allowedDownloadRedirect(latest, releaseUrl("other", "other.db.gz")))
        assertTrue(allowedRedirect(URI("https://release-assets.githubusercontent.com/a?token=test")))
        assertTrue(allowedRedirect(URI("https://fixture.example/a"), setOf("fixture.example")))
        for (bad in listOf(
            "https://github.com.evil.example/zhufree/SCP-Spider/releases/download/tag/file",
            "https://evil.example/zhufree/SCP-Spider/releases/download/tag/file",
            "http://github.com/zhufree/SCP-Spider/releases/download/tag/file",
            "https://github.com/other/SCP-Spider/releases/download/tag/file",
            "https://github.com/zhufree/SCP-Spider/releases/download/tag/file?abc"
        )) assertFalse(isReleaseUrl(URI(bad)))
        for (bad in listOf(
            "https://release-assets.githubusercontent.com.evil.test/a", "http://objects.githubusercontent.com/a",
            "https://evil.test@objects.githubusercontent.com/a", "https://objects.githubusercontent.com:444/a",
            "https://evil.test/a", "//objects.githubusercontent.com/a", "https://objects.githubusercontent.com/a#fragment"
        )) assertFalse(allowedRedirect(URI(bad)))

        for (bad in listOf("..", ".", "../tag", "a/b", "%2e%2e", "tag%2fpath", "name?x", "a\\b")) {
            assertThrows(IllegalArgumentException::class.java) { releaseUrl(bad, "manifest.json") }
            assertThrows(IllegalArgumentException::class.java) { releaseUrl("tag", bad) }
        }
    }

    @Test fun calculatesAndChecksSha256() {
        val file = temp.newFile().apply { writeText("abc") }
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertEquals(expected, sha256(file))
        verifySha256(file, expected)
        assertThrows(IllegalArgumentException::class.java) { verifySha256(file, hash) }
    }

    @Test fun checksColumnNamesTypesNullabilityAndPrimaryKeys() {
        assertTrue(matchesRoomColumns(CATALOG_COLUMNS, CATALOG_COLUMNS.entries.reversed().associate { it.toPair() }))
        assertFalse(matchesRoomColumns(CATALOG_COLUMNS, CATALOG_COLUMNS - "title"))
        assertFalse(matchesRoomColumns(CATALOG_COLUMNS, CATALOG_COLUMNS + ("extra" to RoomColumn("TEXT"))))
        assertFalse(matchesRoomColumns(DETAIL_COLUMNS, DETAIL_COLUMNS + ("not_found" to RoomColumn("TEXT"))))
        assertFalse(matchesRoomColumns(DETAIL_COLUMNS, DETAIL_COLUMNS + ("link" to RoomColumn("TEXT", true))))
        assertFalse(matchesRoomColumns(DETAIL_COLUMNS, DETAIL_COLUMNS + ("tags" to RoomColumn("TEXT", true))))
    }

    @Test fun checksDiskThresholdAndDailyInterval() {
        assertTrue(enoughPartSpace(100 * MIB, 350 * MIB))
        assertFalse(enoughPartSpace(100 * MIB, 350 * MIB - 1))
        assertFalse(enoughPartSpace(Long.MAX_VALUE, Long.MAX_VALUE))
        assertFalse(enoughPartSpace(0, 50 * MIB))
        assertFalse(checkDue(1, CHECK_INTERVAL))
        assertTrue(checkDue(1, CHECK_INTERVAL + 1))
        assertTrue(checkDue(0, 1))
        assertFalse(checkDue(10, 9))
    }

    @Test fun plansInstallsReplacementsSkipsAndRetirementWhenPartsVersionChanges() {
        val part = parseManifest(manifest()).parts.single()
        val installed = mapOf(
            part.id to InstalledPart("7", part.digest),
            "changed" to InstalledPart("7", "previous"),
            "unlisted" to InstalledPart("7", part.digest)
        )
        val assets = listOf(part, part.copy(id = "changed"), part.copy(id = "new"))
        assertEquals(mapOf("misc" to PartAction.SKIP, "changed" to PartAction.REPLACE, "new" to PartAction.INSTALL),
            planParts("7", assets, installed))
        assertEquals(mapOf("misc" to PartAction.REPLACE, "changed" to PartAction.REPLACE,
            "new" to PartAction.INSTALL, "unlisted" to PartAction.DELETE), planParts("8", assets, installed))
    }

    @Test fun installsAndRemovesOldSidecars() {
        val directory = temp.newFolder()
        val live = directory.resolve("catalog.db").apply { writeText("old") }
        val staging = directory.resolve("catalog.staging").apply { writeText("new") }
        val sidecars = listOf("-wal", "-shm", "-journal").map { directory.resolve(live.name + it).apply { writeText("old") } }
        DatabaseSwap(live, staging).install { assertEquals("new", it.readText()) }
        assertEquals("new", live.readText())
        assertFalse(staging.exists())
        assertFalse(directory.resolve("catalog.db.bak").exists())
        assertTrue(sidecars.none { it.exists() })
    }

    @Test fun restoresOldDatabaseOnFailedProbeAndRecoversInterruptedRename() {
        val directory = temp.newFolder()
        val live = directory.resolve("catalog.db").apply { writeText("old") }
        val staging = directory.resolve("catalog.staging").apply { writeText("new") }
        val backup = directory.resolve("catalog.db.bak")
        assertThrows(IllegalStateException::class.java) {
            DatabaseSwap(live, staging).install { error("invalid staged database") }
        }
        assertEquals("old", live.readText())
        assertFalse(backup.exists())

        moveFile(live, backup)
        staging.writeText("new")
        DatabaseSwap(live, staging).recover { error("missing live should roll back without probing") }
        assertEquals("old", live.readText())
        assertEquals("new", staging.readText())
        assertFalse(backup.exists())
    }

    @Test fun restoresOriginalWhenSidecarRemovalFailsBeforePromotion() {
        val directory = temp.newFolder()
        val live = directory.resolve("catalog.db").apply { writeText("old") }
        val staging = directory.resolve("catalog.staging").apply { writeText("new") }
        val blockedSidecar = directory.resolve("catalog.db-shm").apply { mkdir() }
        blockedSidecar.resolve("child").writeText("prevents deletion")
        assertThrows(IllegalStateException::class.java) { DatabaseSwap(live, staging).install {} }
        assertEquals("old", live.readText())
        assertEquals("new", staging.readText())
        assertFalse(directory.resolve("catalog.db.bak").exists())
    }

    @Test fun recoversInterruptedSwapAfterStagingBecameLive() {
        val directory = temp.newFolder()
        val live = directory.resolve("catalog.db").apply { writeText("new") }
        val backup = directory.resolve("catalog.db.bak").apply { writeText("old") }
        DatabaseSwap(live, directory.resolve("catalog.staging")).recover { assertEquals("new", it.readText()) }
        assertEquals("new", live.readText())
        assertFalse(backup.exists())
    }

    private fun manifest() = """
        {
          "manifest_version": 1, "tag": "db-20261124-1", "export_id": "new-export",
          "contract_version": 1, "parts_version": 7, "exported_at": "2026-11-24T12:00:00Z",
          "assets": [
            {"kind":"category","name":"category.db.gz","release_tag":"db-20261124-1",
             "size":12,"raw_size":20,"sha256":"$hash","raw_sha256":"$hash","url":"http://evil.test"},
            {"kind":"part","name":"detail-misc-aabb.db.gz","release_tag":"detail-parts",
             "size":13,"raw_size":21,"sha256":"$hash","raw_sha256":"$hash",
             "part_id":"misc","label":"其他","rows":3,"content_digest":"$hash","part_export_id":"reused-export"}
          ]
        }
    """.trimIndent()
}
