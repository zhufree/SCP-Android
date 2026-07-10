package info.free.scp.data.random

import info.free.scp.bean.ScpItemModel
import org.junit.Assert.assertEquals
import org.junit.Test

class RandomRepositoryTest {
    @Test
    fun refreshReplacesCurrentList() {
        val source = FakeRandomDataSource(
            batches = listOf(listOf(document("/scp-002", "SCP-002")))
        )
        val repository = RandomRepository(source)

        val result = repository.getRandomList(
            range = "1,2",
            currentList = listOf(document("/scp-001", "SCP-001"))
        )

        assertEquals(listOf("/scp-002"), result.map { it.link })
        assertEquals(listOf("1,2"), source.requestedRanges)
    }

    @Test
    fun addMoreAppendsNextBatchInOrder() {
        val source = FakeRandomDataSource(
            batches = listOf(
                listOf(
                    document("/scp-003", "SCP-003"),
                    document("/scp-004", "SCP-004")
                )
            )
        )
        val repository = RandomRepository(source)

        val result = repository.getRandomList(
            range = "7,8",
            currentList = listOf(document("/scp-001", "SCP-001")),
            append = true
        )

        assertEquals(listOf("/scp-001", "/scp-003", "/scp-004"), result.map { it.link })
        assertEquals(listOf("7,8"), source.requestedRanges)
    }

    @Test
    fun hideFinishedFiltersReadDocuments() {
        val source = FakeRandomDataSource(
            batches = listOf(
                listOf(
                    document("/scp-005", "SCP-005"),
                    document("/scp-006", "SCP-006")
                )
            ),
            hideFinished = true,
            readLinks = setOf("/scp-005")
        )
        val repository = RandomRepository(source)

        val result = repository.getRandomList(range = "")

        assertEquals(listOf("/scp-006"), result.map { it.link })
    }

    @Test
    fun missingOfflineDetailIsFilteredOut() {
        val source = FakeRandomDataSource(
            batches = listOf(
                listOf(
                    document("/scp-007", "SCP-007"),
                    document("/scp-008", "SCP-008")
                )
            ),
            availableDetailLinks = setOf("/scp-008")
        )
        val repository = RandomRepository(source)

        val result = repository.getRandomList(range = "")

        assertEquals(listOf("/scp-008"), result.map { it.link })
    }

    @Test
    fun filteredBatchIsRefilledToRequestedSize() {
        val source = FakeRandomDataSource(
            batches = listOf(
                listOf(
                    document("/scp-009", "SCP-009"),
                    document("/scp-010", "SCP-010")
                ),
                listOf(document("/scp-011", "SCP-011"))
            ),
            availableDetailLinks = setOf("/scp-010", "/scp-011")
        )
        val repository = RandomRepository(source, batchSize = 2)

        val result = repository.getRandomList(range = "3,4")

        assertEquals(listOf("/scp-010", "/scp-011"), result.map { it.link })
        assertEquals(listOf("3,4", "3,4"), source.requestedRanges)
    }

    private fun document(link: String, title: String): ScpItemModel {
        return ScpItemModel().apply {
            this.link = link
            this.title = title
        }
    }
}

private class FakeRandomDataSource(
    private val batches: List<List<ScpItemModel>>,
    private val hideFinished: Boolean = false,
    private val readLinks: Set<String> = emptySet(),
    private val availableDetailLinks: Set<String>? = null
) : RandomDataSource {
    val requestedRanges = mutableListOf<String>()
    private var nextBatchIndex = 0

    override fun getRandomDocuments(range: String, count: Int): List<ScpItemModel> {
        requestedRanges.add(range)
        return batches.getOrNull(nextBatchIndex++).orEmpty().take(count)
    }

    override fun shouldHideFinished(): Boolean = hideFinished

    override fun getReadLinks(): Set<String> = readLinks

    override fun hasOfflineDetail(link: String): Boolean {
        return availableDetailLinks?.contains(link) ?: true
    }
}
