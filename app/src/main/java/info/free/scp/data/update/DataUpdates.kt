package info.free.scp.data.update

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.net.URI
import java.util.zip.GZIPInputStream

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
        val due = checkDue(prefs.getLong(LAST_CHECK, 0L), now)
        if (network().connected && due) {
            check(prefs.edit().putLong(LAST_CHECK, now).commit()) { "无法保存数据检查时间" }
            val temporary = File.createTempFile("scp-update-", ".part", context.cacheDir)
            try {
                downloadReleaseFile(URI(MANIFEST_URL), temporary, MANIFEST_LIMIT)
                val parsed = parseManifest(temporary.readText(Charsets.UTF_8))
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
            state.value.catalogOffer != null && !connection.metered -> stageCatalog(checkNotNull(manifest), false)
            state.value.catalogOffer != null -> status("点击检查数据更新，确认使用计费网络下载目录库")
            manual && state.value.stagedCatalog != null -> status("目录更新将在下次冷启动时生效")
            manual && !due -> status(if (manifest != null) "24 小时内已检查过，当前显示上次结果" else "24 小时内已尝试检查，请稍后再试")
            manual -> status("目录已是最新")
            else -> status("")
        }
    }

    fun downloadCatalog() = runOperation("正在准备下载目录库…") {
        loadCachedManifest()
        publishLocalState()
        if (state.value.catalogOffer != null) stageCatalog(checkNotNull(manifest), true)
        else status("没有待下载的目录更新")
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
        check(!state.value.detailInventoryUnavailable) { "无法读取本地正文库" }
        check(!state.value.obsoletePartsManifest) { "清单分块规则早于设备已安装的版本" }
        check(!databases.legacyDetailPending) { "请先重启应用以完成整库导入" }
        requirePartNetwork(network())
        val storage = databases.detailFile.parentFile?.takeIf(File::isDirectory) ?: context.filesDir
        check(enoughPartSpace(part.file.rawSize, storage.usableSpace)) {
            "可用空间不足，需要正文块原始大小的 3 倍加 50 MB"
        }
        check(context.cacheDir.usableSpace >= part.file.size) { "缓存目录空间不足" }
        withAsset(part.file, PART_LIMIT, { requirePartNetwork(it) }) { file ->
            status("正在校验正文块…")
            importer.validate(file, current, part)
            status("正在合并正文块…")
            importer.install(file, current, part)
        }
        publishLocalState()
        status("正文块已安装")
    }

    private fun stageCatalog(current: UpdateManifest, allowMetered: Boolean) {
        withAsset(current.catalog, CATEGORY_LIMIT, {
            check(it.connected && (allowMetered || !it.metered)) { "网络已变更，请重新确认目录下载" }
        }) { file ->
            status("正在校验目录库…")
            databases.prepareCatalog(file, current.catalogVersion)
        }
        publishLocalState()
        status("目录更新将在下次冷启动时生效")
    }

    private fun withAsset(
        asset: UpdateFile, limit: Long, requireNetwork: (UpdateNetwork) -> Unit, action: (File) -> Unit
    ) {
        requireNetwork(network())
        val zipped = File.createTempFile("scp-update-", ".part", context.cacheDir)
        try {
            var previous = -1
            downloadReleaseFile(asset.url, zipped, limit, asset.size) { bytes ->
                val percent = (bytes * 100 / asset.size).toInt()
                if (percent != previous) {
                    requireNetwork(network())
                    status("正在下载 ${percent}%")
                    previous = percent
                }
            }
            verifySha256(zipped, asset.sha256)
            val unpacked = databases.newDatabaseTemp()
            try {
                status("正在解压并校验文件…")
                val size = zipped.inputStream().buffered().use { input ->
                    GZIPInputStream(input).use { copyBounded(it, unpacked, asset.rawSize) }
                }
                require(size == asset.rawSize) { "解压后的文件大小不匹配" }
                verifySha256(unpacked, asset.rawSha256)
                action(unpacked)
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
        if (manifest == null && manifestFile.isFile && manifestFile.length() in 1..MANIFEST_LIMIT) {
            manifest = runCatching { parseManifest(manifestFile.readText(Charsets.UTF_8)) }.getOrNull()
        }
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
        mutableState.value = state.value.copy(
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

    private fun runOperation(progress: String, action: () -> Unit) {
        scope.launch {
            operations.withLock {
                try {
                    mutableState.value = state.value.copy(busy = true, status = progress, failed = false)
                    action()
                } catch (failure: Exception) {
                    runCatching { publishLocalState() }
                    reportFailure(failure)
                } finally {
                    mutableState.value = state.value.copy(busy = false)
                }
            }
        }
    }

    private fun reportFailure(failure: Throwable) {
        // Do not log signed redirect URLs from network exceptions.
        Log.w(UPDATE_LOG, "Data update failed: ${failure.javaClass.simpleName}")
        status(when (failure) {
            is java.io.IOException -> "数据下载或文件读写失败"
            is IllegalArgumentException, is IllegalStateException -> failure.message ?: "数据更新失败"
            else -> "数据更新失败"
        })
        mutableState.value = state.value.copy(failed = true)
    }

    private fun status(message: String) {
        mutableState.value = state.value.copy(status = message, failed = false)
    }

    @Suppress("DEPRECATION")
    private fun network(): UpdateNetwork {
        val active = connectivity.activeNetworkInfo
        return UpdateNetwork(active?.isConnected == true, connectivity.isActiveNetworkMetered,
            active?.type == ConnectivityManager.TYPE_WIFI)
    }

    private data class UpdateNetwork(val connected: Boolean, val metered: Boolean, val wifi: Boolean)
}
