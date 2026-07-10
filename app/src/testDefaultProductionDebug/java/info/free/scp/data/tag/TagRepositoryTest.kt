package info.free.scp.data.tag

import info.free.scp.bean.ScpModel
import info.free.scp.bean.TagModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TagRepositoryTest {
    @Test
    fun localTagSearchQueriesEveryLinkChunk() = runBlocking {
        val links = (1..21).map { "/scp-$it" }
        val source = FakeTagDataSource().apply {
            offlineDetailData = true
            linksByTag["%safe%"] = links
        }
        val repository = TagRepository(source)

        val result = repository.getScpByTag("safe")

        assertEquals(links, result.map { it.link })
        assertEquals(listOf(10, 10, 1), source.documentLinkQueries.map { it.size })
    }

    @Test
    fun remoteTagSearchIsUsedWhenOfflineDetailsAreUnavailable() = runBlocking {
        val source = FakeTagDataSource().apply {
            remoteDocumentsByTag["safe"] = listOf(document("/remote", "Remote"))
        }
        val repository = TagRepository(source)

        val result = repository.getScpByTag("safe")

        assertEquals(listOf("/remote"), result.map { it.link })
    }

    @Test
    fun localTagsCountEveryOccurrence() = runBlocking {
        val source = FakeTagDataSource().apply {
            offlineDetailData = true
            localTagStrings = listOf("safe,euclid", "safe", "", null)
        }
        val repository = TagRepository(source)

        val result = repository.getTags()

        assertEquals(mapOf("safe" to 2, "euclid" to 1), result)
    }

    @Test
    fun remoteTagsAreUsedWhenOfflineDetailsAreUnavailable() = runBlocking {
        val source = FakeTagDataSource().apply {
            remoteTags = listOf(TagModel("safe", 5), TagModel("euclid", 3))
        }
        val repository = TagRepository(source)

        val result = repository.getTags()

        assertEquals(mapOf("safe" to 5, "euclid" to 3), result)
    }

    @Test
    fun tagTextDelegatesToLocalDataSource() {
        val source = FakeTagDataSource().apply {
            tagTextByLink["/scp-001"] = "safe,euclid"
        }
        val repository = TagRepository(source)

        assertEquals("safe,euclid", repository.getTagText("/scp-001"))
    }

    private fun document(link: String, title: String): ScpModel {
        return ScpModel(link = link, title = title)
    }
}

private class FakeTagDataSource : TagDataSource {
    var offlineDetailData = false
    val linksByTag = mutableMapOf<String, List<String>>()
    val documentLinkQueries = mutableListOf<List<String>>()
    var localTagStrings = emptyList<String?>()
    val tagTextByLink = mutableMapOf<String, String>()
    val remoteDocumentsByTag = mutableMapOf<String, List<ScpModel>>()
    var remoteTags = emptyList<TagModel>()

    override fun hasOfflineDetailData(): Boolean {
        return offlineDetailData
    }

    override fun getLinksByTag(tagPattern: String): List<String> {
        return linksByTag[tagPattern].orEmpty()
    }

    override fun getDocumentsByLinks(links: List<String>): List<ScpModel> {
        documentLinkQueries.add(links)
        return links.map { document(it, it) }
    }

    override fun getAllTagStrings(): List<String?> {
        return localTagStrings
    }

    override fun getTagText(link: String): String? {
        return tagTextByLink[link]
    }

    override suspend fun fetchDocumentsByTag(tag: String): List<ScpModel> {
        return remoteDocumentsByTag[tag].orEmpty()
    }

    override suspend fun fetchAllTags(): List<TagModel> {
        return remoteTags
    }

    private fun document(link: String, title: String): ScpModel {
        return ScpModel(link = link, title = title)
    }
}
