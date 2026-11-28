package info.free.scp.data.update

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.net.URI
import java.util.zip.GZIPOutputStream

class ReleaseDownloadsTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun assetHashFailureFallsBackAndRemembersTheWorkingSource() {
        val bytes = "database".toByteArray()
        val original = temp.newFile().apply { writeBytes(bytes) }
        val goodArchive = temp.newFile()
        GZIPOutputStream(goodArchive.outputStream()).use { it.write(bytes) }
        val asset = UpdateFile(
            "category.db.gz", "db-20261126-1", goodArchive.length(), sha256(goodArchive),
            original.length(), sha256(original)
        )
        val zipped = temp.newFile()
        val unpacked = temp.newFile()
        val sources = ReleaseSources()
        val requested = mutableListOf<URI>()

        repeat(2) {
            sources.fetch { source ->
                downloadVerifiedAsset(source, asset, zipped, unpacked, {}, {}, {}, {}) { url, file, _, _, _ ->
                    requested += url
                    val archive = goodArchive.readBytes()
                    if (source == ReleaseSource.R2) archive[0] = 0
                    file.writeBytes(archive)
                }
            }
        }

        assertEquals(listOf(URI("https://scp-data.zhufree.fun/db-20261126-1/category.db.gz"), asset.url, asset.url), requested)
        assertArrayEquals(bytes, unpacked.readBytes())
        assertFalse(allowedDownloadRedirect(ReleaseSource.R2.manifestUrl, asset.url))
        assertFalse(allowedDownloadRedirect(ReleaseSource.R2.assetUrl(asset), URI("https://objects.githubusercontent.com/asset")))
    }

    @Test fun cancelledTransferAndFailedPreflightDoNotFallBack() {
        for (failure in listOf(CancellationException(), IOException("No space left"), IllegalStateException("Wi-Fi required"))) {
            val requested = mutableListOf<ReleaseSource>()
            assertThrows(failure.javaClass) {
                ReleaseSources().fetch { source ->
                    requested += source
                    throw failure
                }
            }
            assertEquals(listOf(ReleaseSource.R2), requested)
        }

        val requested = mutableListOf<ReleaseSource>()
        assertThrows(CancellationException::class.java) {
            ReleaseSources().fetch(preflight = { if (requested.isNotEmpty()) throw CancellationException() }) { source ->
                requested += source
                throw ReleaseSourceFailure("timeout")
            }
        }
        assertEquals(listOf(ReleaseSource.R2), requested)
    }

    @Test fun missingOrInvalidPrimaryManifestFallsBack() {
        val target = temp.newFile()
        val valid = javaClass.getResourceAsStream("/spider-release/manifest.json")
            ?.bufferedReader()?.use { it.readText() } ?: error("Missing manifest fixture")
        for (unreachable in listOf(true, false)) {
            val requested = mutableListOf<URI>()
            val result = ReleaseSources().fetchManifest(target, {}) { source, file ->
                requested += source.manifestUrl
                when (source) {
                    ReleaseSource.R2 -> if (unreachable) throw ReleaseSourceFailure("HTTP 403") else file.writeText("{")
                    ReleaseSource.GITHUB -> file.writeText(valid)
                }
            }
            assertEquals(parseManifest(valid), result)
            assertEquals(listOf(URI(R2_MANIFEST_URL), URI(MANIFEST_URL)), requested)
        }
    }
}
