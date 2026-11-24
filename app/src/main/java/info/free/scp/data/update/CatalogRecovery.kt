package info.free.scp.data.update

import java.io.File

/** Called only after staged installation and with no long-lived Room connection. */
internal fun recoverCatalog(
    live: File,
    probe: (File) -> Unit,
    installBundled: () -> Unit
): Boolean {
    val hadCatalog = live.exists()
    if (hadCatalog && runCatching { probe(live) }.isSuccess) return false
    quarantineDatabase(live)
    installBundled()
    probe(live)
    return hadCatalog
}

internal fun quarantineDatabase(live: File) {
    val corrupt = File(live.path + ".corrupt")
    if (live.exists()) {
        deleteFile(corrupt)
        deleteSidecars(corrupt)
        moveFile(live, corrupt)
    }
    // An old WAL must never be replayed over a replacement database.
    databaseSidecars.forEach { suffix ->
        val sidecar = File(live.path + suffix)
        if (sidecar.exists()) moveFile(sidecar, File(corrupt.path + suffix))
    }
}
