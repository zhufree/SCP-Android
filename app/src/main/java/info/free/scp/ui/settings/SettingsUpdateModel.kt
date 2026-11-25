package info.free.scp.ui.settings

import info.free.scp.data.update.DataUpdateState
import info.free.scp.data.update.PartAction

/** 正文分块分组顶部的状态横幅，顺序和条件与旧版 SettingsActivity.renderUpdates 的脚注一致。 */
internal enum class PartsBanner {
    INVENTORY_UNAVAILABLE,
    FULL_IMPORT,
    LEGACY_PENDING,
    REINSTALL,
    OBSOLETE
}

internal fun partsBanners(state: DataUpdateState): List<PartsBanner> = listOfNotNull(
    PartsBanner.INVENTORY_UNAVAILABLE.takeIf { state.detailInventoryUnavailable },
    PartsBanner.FULL_IMPORT.takeIf { state.fullDetail },
    PartsBanner.LEGACY_PENDING.takeIf { state.legacyDetailPending },
    PartsBanner.REINSTALL.takeIf { state.reinstallParts && !state.obsoletePartsManifest },
    PartsBanner.OBSOLETE.takeIf { state.obsoletePartsManifest }
)

internal enum class PartRowStatus { UNKNOWN, INSTALLED, UPDATE, NOT_INSTALLED }

internal fun partRowStatus(state: DataUpdateState, partId: String): PartRowStatus =
    when (state.partActions[partId]) {
        null -> PartRowStatus.UNKNOWN
        PartAction.SKIP -> PartRowStatus.INSTALLED
        PartAction.REPLACE -> PartRowStatus.UPDATE
        else -> PartRowStatus.NOT_INSTALLED
    }

/** 分块行能否点击：与旧版 `row.isEnabled` 相同（已装的行不可点）。 */
internal fun isPartActionEnabled(state: DataUpdateState, partId: String): Boolean =
    !state.busy && !state.legacyDetailPending && !state.detailInventoryUnavailable &&
        !state.obsoletePartsManifest && state.partActions[partId] != PartAction.SKIP

internal fun installedPartCount(state: DataUpdateState): Int =
    state.parts.count { state.partActions[it.id] == PartAction.SKIP }
