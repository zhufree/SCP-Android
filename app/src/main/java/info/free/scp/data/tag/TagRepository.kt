package info.free.scp.data.tag

import apiCall
import info.free.scp.bean.ScpModel
import info.free.scp.bean.TagModel
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager

interface TagDataSource {
    fun hasOfflineDetailData(): Boolean
    fun getLinksByTag(tagPattern: String): List<String>
    fun getDocumentsByLinks(links: List<String>): List<ScpModel>
    fun getAllTagStrings(): List<String?>
    fun getTagText(link: String): String?
    suspend fun fetchDocumentsByTag(tag: String): List<ScpModel>
    suspend fun fetchAllTags(): List<TagModel>
}

class RoomTagDataSource : TagDataSource {
    private val scpDao by lazy { ScpDatabase.getInstance()?.scpDao() }
    private val detailDao by lazy { DetailDatabase.getInstance()?.detailDao() }

    override fun hasOfflineDetailData(): Boolean {
        return detailDao != null
    }

    override fun getLinksByTag(tagPattern: String): List<String> {
        return detailDao?.getLinksByTag(tagPattern).orEmpty()
    }

    override fun getDocumentsByLinks(links: List<String>): List<ScpModel> {
        if (links.isEmpty()) return emptyList()
        return scpDao?.getScpListByLinks(links).orEmpty()
    }

    override fun getAllTagStrings(): List<String?> {
        return detailDao?.getAllTags().orEmpty()
    }

    override fun getTagText(link: String): String? {
        return detailDao?.getTag(link)
    }

    override suspend fun fetchDocumentsByTag(tag: String): List<ScpModel> {
        return apiCall { HttpManager.instance.getScpByTag(tag) }?.results.orEmpty()
    }

    override suspend fun fetchAllTags(): List<TagModel> {
        return apiCall { HttpManager.instance.getAllTags() }?.results.orEmpty()
    }
}

class TagRepository(
    private val dataSource: TagDataSource = RoomTagDataSource()
) {
    suspend fun getScpByTag(tag: String): List<ScpModel> {
        if (!dataSource.hasOfflineDetailData()) {
            return dataSource.fetchDocumentsByTag(tag)
        }

        return dataSource.getLinksByTag("%$tag%")
            .chunked(10)
            .flatMap { links -> dataSource.getDocumentsByLinks(links) }
    }

    suspend fun getTags(): Map<String, Int> {
        if (!dataSource.hasOfflineDetailData()) {
            return dataSource.fetchAllTags().associate { it.name to it.count }
        }

        val tagCounts = linkedMapOf<String, Int>()
        dataSource.getAllTagStrings()
            .filterNot { it.isNullOrEmpty() }
            .forEach { tagString ->
                tagString?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.forEach { tag ->
                        tagCounts[tag] = tagCounts.getOrDefault(tag, 0) + 1
                    }
            }
        return tagCounts
    }

    fun getTagText(link: String): String? {
        return dataSource.getTagText(link)
    }
}
