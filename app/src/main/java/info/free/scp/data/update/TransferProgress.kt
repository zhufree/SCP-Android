package info.free.scp.data.update

internal enum class TransferKind { CATALOG, SINGLE_PART, ALL_PARTS }
internal enum class TransferPhase { DOWNLOADING, VERIFYING, MERGING }

internal data class TransferProgress(
    val kind: TransferKind,
    val partIndex: Int,
    val partCount: Int,
    val partLabel: String,
    val receivedBytes: Long,
    val totalBytes: Long,
    val phase: TransferPhase = TransferPhase.DOWNLOADING,
    val bytesPerSecond: Long? = null,
    val remainingSeconds: Long? = null,
    val rateSampleElapsedMillis: Long? = null,
    val switchingSource: Boolean = false
) {
    val fraction: Float get() = (receivedBytes.toDouble() / totalBytes).toFloat()
    val percent: Int get() = (receivedBytes * 100 / totalBytes).toInt()
}

internal class TransferTracker(
    private val kind: TransferKind,
    private val sizes: List<Long>,
    private val nowMillis: () -> Long,
    private val publish: (TransferProgress) -> Unit
) {
    private val total = sizes.sum()
    private var priorBytes = 0L
    private var remainingAfterPart = total
    private var partSize = 0L
    private var highWater = 0L
    private var lastPublished = 0L
    private var sampleTime = 0L
    private var sampleBytes = 0L
    private lateinit var progress: TransferProgress

    fun startPart(index: Int, label: String) {
        priorBytes += highWater
        highWater = 0
        partSize = sizes[index - 1]
        remainingAfterPart -= partSize
        progress = TransferProgress(kind, index, sizes.size, label, priorBytes, total)
        startAttempt(switchingSource = false)
    }

    fun startAttempt(switchingSource: Boolean) {
        val now = nowMillis()
        lastPublished = now
        sampleTime = now
        sampleBytes = 0
        emit(TransferPhase.DOWNLOADING, switchingSource)
    }

    fun onBytes(bytes: Long) {
        // Keep the displayed offset across attempts. Credit the final byte after integrity checks.
        highWater = maxOf(highWater, minOf(bytes, partSize - 1))
        val now = nowMillis()
        if (now - lastPublished < 150 && bytes != partSize) return
        lastPublished = now
        val window = now - sampleTime
        val speed = if (window >= 1_000) {
            val measured = (bytes - sampleBytes) * 1_000 / window
            sampleTime = now
            sampleBytes = bytes
            measured.takeIf { it > 0 }
        } else progress.bytesPerSecond
        val remaining = remainingAfterPart + partSize - bytes
        emit(
            TransferPhase.DOWNLOADING,
            switchingSource = progress.switchingSource && bytes == 0L,
            speed = speed,
            eta = speed?.takeIf { remaining > 0 }?.let { (remaining + it - 1) / it }
        )
    }

    fun verifying() = emit(TransferPhase.VERIFYING)

    fun verified() {
        highWater = partSize
        emit(TransferPhase.VERIFYING)
    }

    fun merging() = emit(TransferPhase.MERGING)

    private fun emit(
        phase: TransferPhase,
        switchingSource: Boolean = false,
        speed: Long? = null,
        eta: Long? = null
    ) {
        progress = progress.copy(
            receivedBytes = priorBytes + highWater,
            phase = phase,
            switchingSource = switchingSource,
            bytesPerSecond = speed,
            remainingSeconds = eta,
            rateSampleElapsedMillis = sampleTime.takeIf { speed != null }
        )
        publish(progress)
    }
}
