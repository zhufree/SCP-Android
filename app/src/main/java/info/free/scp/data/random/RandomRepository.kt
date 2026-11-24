package info.free.scp.data.random

import info.free.scp.bean.ScpItemModel
import info.free.scp.data.catalog.distinctDocuments
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.normalizeLink

interface RandomDataSource {
    fun getRandomDocuments(range: String, count: Int): List<ScpItemModel>
    fun getReadLinks(): Set<String>
    fun shouldHideFinished(): Boolean
    fun hasOfflineDetail(link: String): Boolean
}

class RoomRandomDataSource : RandomDataSource {
    private val scpDao by lazy { ScpDatabase.getInstance()?.scpDao() }
    private val detailDao by lazy { DetailDatabase.getInstance()?.detailDao() }
    private val likeDao get() = AppInfoDatabase.getInstance().likeAndReadDao()

    override fun getRandomDocuments(range: String, count: Int): List<ScpItemModel> {
        if (count <= 0) return emptyList()
        val documents = if (range.isEmpty()) {
            scpDao?.getRandomScp(count)
        } else {
            val rangeParts = range.split(",")
            scpDao?.getRandomScpByType(rangeParts[0], rangeParts[1], count)
        }
        return documents?.toList().orEmpty()
    }

    override fun getReadLinks(): Set<String> {
        return likeDao.getHasReadList().mapTo(mutableSetOf()) { it.link }
    }

    override fun shouldHideFinished(): Boolean {
        return PreferenceUtil.getHideFinished()
    }

    override fun hasOfflineDetail(link: String): Boolean {
        val dao = detailDao ?: return true
        val detail = dao.getDetail(normalizeLink(link))
        return !detail.isNullOrEmpty() && detail != "null"
    }
}

class RandomRepository(
    private val dataSource: RandomDataSource = RoomRandomDataSource(),
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
    private val maxRefillAttempts: Int = MAX_REFILL_ATTEMPTS
) {
    fun getRandomList(
        range: String,
        currentList: List<ScpItemModel> = emptyList(),
        append: Boolean = false
    ): List<ScpItemModel> {
        val previous = if (append) currentList.distinctDocuments() else emptyList()
        return previous + loadBatch(range, previous.mapTo(mutableSetOf()) { normalizeLink(it.link) })
    }

    private fun loadBatch(range: String, seen: MutableSet<String>): List<ScpItemModel> {
        val hideFinished = dataSource.shouldHideFinished()
        val readLinks = if (hideFinished) dataSource.getReadLinks() else emptySet()
        val result = mutableListOf<ScpItemModel>()
        var attempts = 0

        while (result.size < batchSize && attempts < maxRefillAttempts) {
            val requestedCount = batchSize - result.size
            val candidates = dataSource.getRandomDocuments(range, requestedCount)
            if (candidates.isEmpty()) break

            candidates.forEach { document ->
                val key = normalizeLink(document.link)
                if (key !in readLinks && key !in seen && dataSource.hasOfflineDetail(document.link)) {
                    seen.add(key)
                    result.add(document)
                }
            }
            attempts += 1

            if (candidates.size < requestedCount) break
        }
        return result.take(batchSize)
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE = 10
        const val MAX_REFILL_ATTEMPTS = 20
    }
}
