package info.free.scp.data.backup

import info.free.scp.data.update.deleteFile
import info.free.scp.data.update.moveFile
import java.io.File

/** The pending directory is published atomically after all files have been validated. */
internal class RestoreSwap(private val pending: File, private val targets: List<File>) {
    private val journal = File(pending, "originals")
    private val finished = File(pending.path + ".finished")
    private fun staged(target: File) = File(pending, target.name)
    private fun backup(target: File) = File(pending, target.name + ".bak")

    fun install(
        prepare: () -> Unit,
        clearSidecars: (File) -> Unit,
        probe: () -> Unit,
        report: (String) -> Unit
    ) {
        cleanFinished()
        if (!pending.isDirectory) return
        if (journal.isFile) {
            rollback(clearSidecars)
            finish()
            report("上次用户数据恢复中断，已还原原数据")
            return
        }

        val missing = targets.firstOrNull { !staged(it).isFile }
        if (missing != null) {
            finish()
            report("待恢复文件缺失：${missing.name}，请重新选择备份")
            return
        }
        try {
            prepare()
            val originals = targets.filter(File::exists)
            // Keep originals at their live paths until atomic promotion, even if backup copying fails.
            targets.forEach { target ->
                val saved = backup(target)
                deleteFile(saved)
                if (target in originals) {
                    target.inputStream().use { input ->
                        saved.outputStream().use { output ->
                            input.copyTo(output)
                            output.fd.sync()
                        }
                    }
                }
            }
            val temporary = File(pending, "originals.tmp")
            temporary.outputStream().use { output ->
                output.write(originals.joinToString("\n") { it.name }.toByteArray())
                output.fd.sync()
            }
            moveFile(temporary, journal)
            targets.forEach { target ->
                clearSidecars(target)
                moveFile(staged(target), target)
            }
            probe()
            // This rename commits the swap. Cleanup may be retried without rolling back a success.
            moveFile(pending, finished)
        } catch (failure: Exception) {
            if (journal.isFile) {
                try {
                    rollback(clearSidecars)
                    finish()
                } catch (rollbackFailure: Exception) {
                    rollbackFailure.addSuppressed(failure)
                    throw IllegalStateException("用户数据回滚未完成，请重启应用重试", rollbackFailure)
                }
                report("用户数据恢复失败，已还原原数据：${failure.message ?: "文件读写失败"}")
            } else {
                report("用户数据恢复尚未开始，将在下次冷启动重试：${failure.message ?: "文件读写失败"}")
            }
            return
        }
        report("用户数据恢复完成")
        cleanFinished()
    }

    private fun rollback(clearSidecars: (File) -> Unit) {
        val originals = journal.readLines().toSet()
        var failure: Exception? = null
        targets.forEach { target ->
            val saved = backup(target)
            try {
                if (saved.isFile || target.name !in originals) {
                    clearSidecars(target)
                    if (saved.isFile) moveFile(saved, target) else deleteFile(target)
                }
            } catch (error: Exception) {
                if (failure == null) failure = error else failure.addSuppressed(error)
            }
        }
        failure?.let { throw it }
    }

    private fun finish() {
        moveFile(pending, finished)
        cleanFinished()
    }

    private fun cleanFinished() {
        // A failure here is safe to retry; this directory is never treated as pending.
        if (finished.exists()) finished.deleteRecursively()
    }
}
