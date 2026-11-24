package info.free.scp.data.update

import java.io.File

internal fun deleteSidecars(file: File) {
    for (suffix in listOf("-wal", "-shm", "-journal")) {
        deleteFile(File(file.path + suffix))
    }
}

internal fun deleteFile(file: File) {
    check(!file.exists() || file.delete()) { "无法删除 ${file.name}" }
}

internal fun moveFile(from: File, to: File) {
    check(from.renameTo(to)) { "无法将 ${from.name} 移到 ${to.name}" }
}

/** Both files must be on the same filesystem. Call before opening any long-lived connection. */
internal class DatabaseSwap(val live: File, val staging: File) {
    private val backup = File(live.path + ".bak")

    fun recover(probe: (File) -> Unit) {
        if (!backup.exists()) return
        when {
            !live.exists() -> moveFile(backup, live)
            runCatching { probe(live) }.isSuccess -> deleteFile(backup)
            else -> restoreBackup()
        }
    }

    fun install(probe: (File) -> Unit) {
        recover(probe)
        check(staging.isFile) { "待安装文件缺失" }
        if (live.exists()) moveFile(live, backup)
        var promoted = false
        try {
            deleteSidecars(live)
            moveFile(staging, live)
            promoted = true
            probe(live)
        } catch (failure: Exception) {
            try {
                if (promoted) restoreBackup()
                else if (backup.exists()) moveFile(backup, live)
            } catch (restoreFailure: Exception) {
                failure.addSuppressed(restoreFailure)
            }
            throw failure
        }
        deleteFile(backup)
    }

    private fun restoreBackup() {
        deleteSidecars(live)
        deleteFile(live)
        if (backup.exists()) moveFile(backup, live)
    }
}
