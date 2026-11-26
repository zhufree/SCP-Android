package info.free.scp.data.update

import kotlinx.coroutines.CancellationException

internal fun pendingPartDownloads(parts: List<DetailPart>, actions: Map<String, PartAction>): List<DetailPart> =
    parts.filter { part ->
        val action = actions[part.id]
        action == PartAction.INSTALL || action == PartAction.REPLACE
    }

internal data class PartDownloadFailure(val part: DetailPart, val cause: Exception)

/** Each install commits independently. The caller rebuilds the plan from disk on retry. */
internal fun installDetailParts(
    parts: List<DetailPart>,
    install: (index: Int, part: DetailPart) -> Unit
): List<PartDownloadFailure> = buildList {
    parts.forEachIndexed { index, part ->
        try {
            install(index + 1, part)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            add(PartDownloadFailure(part, failure))
        }
    }
}
