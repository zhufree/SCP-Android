package info.free.scp.data.update

import android.content.Context
import android.net.ConnectivityManager
import android.os.SystemClock
import android.util.Log
import info.free.scp.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

private const val LAST_CHECK = "last_manifest_check"
private const val WIFI_ONLY = "detail_wifi_only"

internal data class DataUpdateState(
    val catalog: CatalogVersion? = null,
    val stagedCatalog: CatalogVersion? = null,
    val catalogOffer: String? = null,
    val confirmMetered: Boolean = false,
    val parts: List<DetailPart> = emptyList(),
    val partActions: Map<String, PartAction> = emptyMap(),
    val fullDetail: Boolean = false,
    val detailInventoryUnavailable: Boolean = false,
    val legacyDetailPending: Boolean = false,
    val reinstallParts: Boolean = false,
    val obsoletePartsManifest: Boolean = false,
    val transfer: TransferProgress? = null,
    val cancellingDownload: Boolean = false,
    val lastTransferKind: TransferKind? = null,
    val busy: Boolean = false,
    val status: String = "",
    /** 最近一次操作失败（[status] 是失败原因），设置页据此显示错误色和“重试”。 */
    val failed: Boolean = false
)

internal class DataUpdates(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val operations = Mutex()
    private val prefs = context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
    private val databases = UpdateDatabases(context)
    private val importer = DetailPartImporter(databases)
    private val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val manifestFile = File(context.filesDir, "scp-update-manifest.json")
    private val mutableState = MutableStateFlow(DataUpdateState())
    val state = mutableState.asStateFlow()

    private var manifest: UpdateManifest? = null
    private var promptedExport: String? = null
    private val downloadLock = Any()
    private var downloadJob: Job? = null

    var wifiOnly: Boolean
        get() = prefs.getBoolean(WIFI_ONLY, true)
        set(value) { prefs.edit().putBoolean(WIFI_ONLY, value).apply() }

    @Synchronized
    fun claimMeteredPrompt(): Boolean {
        val current = state.value
        val export = current.catalogOffer ?: return false
        if (!current.confirmMetered || promptedExport == export) return false
        promptedExport = export
        return true
    }

    fun canConfirmCachedCatalog(): Boolean =
        state.value.catalogOffer != null && network().let { it.connected && it.metered } &&
            !checkDue(prefs.getLong(LAST_CHECK, 0L), System.currentTimeMillis())

    fun refresh() {
        scope.launch {
            operations.withLock {
                loadCachedManifest()
                runCatching { publishLocalState() }.onFailure { reportFailure(it) }
            }
        }
    }

    fun check(manual: Boolean = false) = runOperation("正在检查数据更新…") {
        loadCachedManifest()
        val now = System.currentTimeMillis()
        val due = manual || shouldCheckManifest(manifestFile, manifest, prefs.getLong(LAST_CHECK, 0L), now)
        val sources = releaseSources()
        val job = coroutineContext.job
        if (network().connected && due) {
            check(prefs.edit().putLong(LAST_CHECK, now).commit()) { "无法保存数据检查时间" }
            val temporary = File.createTempFile("scp-update-", ".part", context.cacheDir)
            try {
                val parsed = sources.fetchManifest(temporary, preflight = {
                    job.ensureActive()
                    check(network().connected) { "请先连接网络" }
                })
                moveFile(temporary, manifestFile)
                manifest = parsed
            } finally {
                deleteFile(temporary)
            }
        }
        publishLocalState()
        val connection = network()
        when {
            !connection.connected -> status(if (manual) "连接网络后可以检查更新" else "")
            state.value.catalogOffer != null && !connection.metered -> stageCatalog(checkNotNull(manifest), false, sources, job)
            state.value.catalogOffer != null -> status("点击检查数据更新，确认使用计费网络下载目录库")
            manual && state.value.stagedCatalog != null -> status("目录更新将在下次冷启动时生效")
            manual -> status("目录已是最新")
            else -> status("")
        }
    }

    fun downloadCatalog() = runOperation("正在准备下载目录库…") {
        loadCachedManifest()
        publishLocalState()
        if (state.value.catalogOffer != null) {
            stageCatalog(checkNotNull(manifest), true, releaseSources(), coroutineContext.job)
        } else {
            status("没有待下载的目录更新")
        }
    }

    fun installPart(partId: String) = runOperation("正在准备下载正文块…") {
        loadCachedManifest()
        val current = checkNotNull(manifest) { "请先检查数据更新" }
        val part = current.parts.firstOrNull { it.id == partId } ?: error("清单中已没有此正文块")
        publishLocalState()
        if (state.value.partActions[partId] == PartAction.SKIP) {
            status("正文块已安装")
            return@runOperation
        }
        require(!part.exceedsSizeLimit) { "正文块超过 200 MiB 下载或解压上限" }
        requirePartInstallReady()
        val job = coroutineContext.job
        val progress = beginTransfer(job, TransferKind.SINGLE_PART, listOf(part.file.size))
        progress.startPart(1, part.label)
        downloadAndInstallPart(current, part, releaseSources(), progress, job::ensureActive)
        publishLocalState()
        status("正文块已安装")
    }

    fun installAllParts() = runOperation("正在准备下载全部正文…") {
        loadCachedManifest()
        val current = checkNotNull(manifest) { "请先检查数据更新" }
        publishLocalState()
        requirePartInstallReady()
        val pending = pendingPartDownloads(current.parts, state.value.partActions)
        if (pending.isEmpty()) return@runOperation status("")

        val job = coroutineContext.job
        val sources = releaseSources()
        val progress = beginTransfer(job, TransferKind.ALL_PARTS, pending.map { it.file.size })
        requirePartNetwork(network())
        val failures = installDetailParts(pending) { index, part ->
            job.ensureActive()
            progress.startPart(index, part.label)
            downloadAndInstallPart(current, part, sources, progress, job::ensureActive)
            publishLocalState()
        }
        job.ensureActive()
        publishLocalState()
        val installed = pending.size - failures.size
        val firstFailure = failures.firstOrNull()
        if (firstFailure == null) {
            status("已安装全部可下载的正文块（$installed 块）")
        } else {
            val reason = failureMessage(firstFailure.cause)
            status("已安装 $installed 块，${failures.size} 块失败。${firstFailure.part.label}：$reason")
            mutableState.update { it.copy(failed = true) }
        }
    }

    fun cancelDownload() {
        synchronized(downloadLock) {
            val job = downloadJob ?: return
            if (state.value.transfer == null) return
            mutableState.update { it.copy(cancellingDownload = true) }
            job.cancel()
        }
    }

    private fun beginTransfer(job: Job, kind: TransferKind, sizes: List<Long>): TransferTracker {
        synchronized(downloadLock) {
            downloadJob = job
            mutableState.update { it.copy(lastTransferKind = kind) }
        }
        return TransferTracker(kind, sizes, SystemClock::elapsedRealtime) { progress ->
            mutableState.update { it.copy(transfer = progress) }
        }
    }

    private fun requirePartInstallReady() {
        check(!state.value.detailInventoryUnavailable) { "无法读取本地正文库" }
        check(!state.value.obsoletePartsManifest) { "清单分块规则早于设备已安装的版本" }
        check(!databases.legacyDetailPending) { "请先重启应用以完成整库导入" }
    }

    private fun releaseSources() = ReleaseSources(
        onAttempt = { debugTransfer("Requesting from $it") },
        onSuccess = { debugTransfer("Verified from $it") }
    )

    private fun debugTransfer(event: String) {
        if (BuildConfig.DEBUG) {
            val transfer = state.value.transfer
            val asset = if (transfer == null) "manifest" else "${transfer.kind} ${transfer.partIndex}/${transfer.partCount} ${transfer.partLabel}"
            Log.d(UPDATE_LOG, "$event: $asset")
        }
    }

    private fun downloadAndInstallPart(
        current: UpdateManifest,
        part: DetailPart,
        sources: ReleaseSources,
        progress: TransferTracker,
        checkCancelled: () -> Unit
    ) {
        requirePartNetwork(network())
        val storage = databases.detailFile.parentFile?.takeIf(File::isDirectory) ?: context.filesDir
        check(enoughPartSpace(part.file.rawSize, storage.usableSpace)) {
            "可用空间不足，需要正文块原始大小的 3 倍加 50 MiB"
        }
        withAsset(part.file, sources, progress, ::requirePartNetwork, checkCancelled) { file ->
            importer.validate(file, current, part)
            checkCancelled()
            progress.merging()
            importer.install(file, current, part)
        }
    }

    private fun stageCatalog(current: UpdateManifest, allowMetered: Boolean, sources: ReleaseSources, job: Job) {
        val progress = beginTransfer(job, TransferKind.CATALOG, listOf(current.catalog.size))
        progress.startPart(1, "")
        withAsset(current.catalog, sources, progress, {
            check(it.connected && (allowMetered || !it.metered)) { "网络已变更，请重新确认目录下载" }
        }, job::ensureActive) { file ->
            databases.prepareCatalog(file, current.catalogVersion) {
                job.ensureActive()
            }
        }
        publishLocalState()
        status("目录更新将在下次冷启动时生效")
    }

    private fun withAsset(
        asset: UpdateFile,
        sources: ReleaseSources,
        progress: TransferTracker,
        requireNetwork: (UpdateNetwork) -> Unit,
        checkCancelled: () -> Unit,
        action: (File) -> Unit
    ) {
        val preflight = {
            checkCancelled()
            requireNetwork(network())
        }
        preflight()
        check(context.cacheDir.usableSpace >= asset.size) { "缓存目录空间不足" }
        val zipped = File.createTempFile("scp-update-", ".part", context.cacheDir)
        try {
            val unpacked = databases.newDatabaseTemp()
            try {
                var lastNetworkCheck = SystemClock.elapsedRealtime()
                sources.fetch(preflight, onFallback = { progress.startAttempt(switchingSource = true) }) { source ->
                    downloadVerifiedAsset(
                        source, asset, zipped, unpacked, preflight, checkCancelled,
                        onBytes = { bytes ->
                            checkCancelled()
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastNetworkCheck >= 250) {
                                preflight()
                                lastNetworkCheck = now
                            }
                            progress.onBytes(bytes)
                        },
                        onVerifying = progress::verifying
                    )
                }
                checkCancelled()
                progress.verified()
                action(unpacked)
                debugTransfer("Applied")
            } finally {
                deleteSidecars(unpacked)
                deleteFile(unpacked)
            }
        } finally {
            deleteFile(zipped)
        }
    }

    private fun requirePartNetwork(current: UpdateNetwork) {
        check(current.connected) { "请先连接网络" }
        check(!wifiOnly || current.wifi) { "请连接 Wi-Fi，或关闭正文仅 Wi-Fi 下载" }
    }

    private fun loadCachedManifest() {
        if (manifest == null) manifest = readCachedManifest(manifestFile)
    }

    private fun publishLocalState() {
        val catalog = runCatching { databases.installedCatalog() }.getOrNull()
        val staged = runCatching { databases.pendingCatalog() }.getOrNull()
        val available = manifest
        val offer = available != null &&
            newerCatalog(available.catalogVersion, catalog) &&
            newerCatalog(available.catalogVersion, staged)
        val inventory = runCatching { importer.inventory() }.getOrNull()
        val installedParts = inventory?.parts.orEmpty()
        val plan = if (available == null || inventory == null) emptyMap()
        else planParts(available.partsVersion, available.parts, installedParts)
        mutableState.update {
            it.copy(
                catalog = catalog,
                stagedCatalog = staged,
                catalogOffer = available?.catalogVersion?.exportId?.takeIf { offer },
                confirmMetered = offer && network().let { it.connected && it.metered },
                parts = available?.parts.orEmpty(),
                partActions = plan,
                fullDetail = inventory?.fullImport == true,
                detailInventoryUnavailable = inventory == null,
                legacyDetailPending = databases.legacyDetailPending,
                reinstallParts = available != null && installedParts.values.any {
                    it.version.toInt() < available.partsVersion.toInt()
                },
                obsoletePartsManifest = available != null && installedParts.values.any {
                    it.version.toInt() > available.partsVersion.toInt()
                }
            )
        }
    }

    private fun runOperation(progress: String, action: CoroutineScope.() -> Unit) {
        scope.launch {
            operations.withLock {
                try {
                    mutableState.update {
                        it.copy(busy = true, status = progress, failed = false, lastTransferKind = null)
                    }
                    action()
                } catch (cancelled: CancellationException) {
                    debugTransfer("Cancelled")
                    runCatching { publishLocalState() }
                    status(when (state.value.lastTransferKind) {
                        TransferKind.CATALOG -> "已取消目录下载"
                        TransferKind.ALL_PARTS -> "已停止批量下载，已安装的正文块已保留"
                        else -> "已取消正文下载"
                    })
                    throw cancelled
                } catch (failure: Exception) {
                    runCatching { publishLocalState() }
                    reportFailure(failure)
                } finally {
                    synchronized(downloadLock) {
                        downloadJob = null
                        mutableState.update { it.copy(busy = false, transfer = null, cancellingDownload = false) }
                    }
                }
            }
        }
    }

    private fun reportFailure(failure: Throwable) {
        status(failureMessage(failure))
        mutableState.update { it.copy(failed = true) }
    }

    private fun failureMessage(failure: Throwable): String {
        // Do not log signed redirect URLs from network exceptions.
        Log.w(UPDATE_LOG, "Data update failed: ${failure.javaClass.simpleName}")
        return when (failure) {
            is java.io.IOException -> "数据下载或文件读写失败"
            is IllegalArgumentException, is IllegalStateException -> failure.message ?: "数据更新失败"
            else -> "数据更新失败"
        }
    }

    private fun status(message: String) {
        mutableState.update { it.copy(status = message, failed = false) }
    }

    @Suppress("DEPRECATION")
    private fun network(): UpdateNetwork {
        val active = connectivity.activeNetworkInfo
        return UpdateNetwork(active?.isConnected == true, connectivity.isActiveNetworkMetered,
            active?.type == ConnectivityManager.TYPE_WIFI)
    }

    private data class UpdateNetwork(val connected: Boolean, val metered: Boolean, val wifi: Boolean)
}
