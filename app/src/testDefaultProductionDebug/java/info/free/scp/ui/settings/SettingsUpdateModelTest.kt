package info.free.scp.ui.settings

import info.free.scp.data.update.DataUpdateState
import info.free.scp.data.update.DetailPart
import info.free.scp.data.update.PartAction
import info.free.scp.data.update.UpdateFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUpdateModelTest {
    private fun part(id: String) = DetailPart(
        id, "分块 $id", 10, "d", "e",
        UpdateFile("$id.db.gz", "tag", 100, "s", 200, "r")
    )

    private val parts = listOf(part("a"), part("b"), part("c"), part("d"))
    private val base = DataUpdateState(
        parts = parts,
        partActions = mapOf("a" to PartAction.SKIP, "b" to PartAction.REPLACE, "c" to PartAction.INSTALL)
    )

    @Test
    fun bannersFollowLegacyOrderAndConditions() {
        assertEquals(emptyList<PartsBanner>(), partsBanners(base))
        val all = base.copy(
            detailInventoryUnavailable = true, fullDetail = true, legacyDetailPending = true, reinstallParts = true
        )
        assertEquals(
            listOf(PartsBanner.INVENTORY_UNAVAILABLE, PartsBanner.FULL_IMPORT, PartsBanner.LEGACY_PENDING, PartsBanner.REINSTALL),
            partsBanners(all)
        )
    }

    @Test
    fun obsoleteManifestSuppressesReinstallBanner() {
        val state = base.copy(reinstallParts = true, obsoletePartsManifest = true)
        assertEquals(listOf(PartsBanner.OBSOLETE), partsBanners(state))
    }

    @Test
    fun rowStatusFromPartActions() {
        assertEquals(PartRowStatus.INSTALLED, partRowStatus(base, "a"))
        assertEquals(PartRowStatus.UPDATE, partRowStatus(base, "b"))
        assertEquals(PartRowStatus.NOT_INSTALLED, partRowStatus(base, "c"))
        assertEquals(PartRowStatus.UNKNOWN, partRowStatus(base, "d"))
    }

    @Test
    fun actionEnabledMatchesLegacyRules() {
        assertFalse(isPartActionEnabled(base, "a"))
        assertTrue(isPartActionEnabled(base, "b"))
        assertTrue(isPartActionEnabled(base, "c"))
        assertTrue(isPartActionEnabled(base, "d"))
        assertFalse(isPartActionEnabled(base.copy(busy = true), "c"))
        assertFalse(isPartActionEnabled(base.copy(legacyDetailPending = true), "c"))
        assertFalse(isPartActionEnabled(base.copy(detailInventoryUnavailable = true), "c"))
        assertFalse(isPartActionEnabled(base.copy(obsoletePartsManifest = true), "c"))
    }

    @Test
    fun installedCountOnlyCountsSkip() {
        assertEquals(1, installedPartCount(base))
        assertEquals(0, installedPartCount(DataUpdateState()))
    }

    @Test
    fun oversizedPartIsUnavailableWithoutDisablingOtherDownloads() {
        val state = base.copy(partActions = base.partActions + ("b" to PartAction.TOO_LARGE))
        assertEquals(PartRowStatus.TOO_LARGE, partRowStatus(state, "b"))
        assertFalse(isPartActionEnabled(state, "b"))
        assertTrue(isPartActionEnabled(state, "c"))
    }
}
