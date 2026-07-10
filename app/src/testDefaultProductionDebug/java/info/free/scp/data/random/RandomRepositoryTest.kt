package info.free.scp.data.random

import info.free.scp.bean.ScpItemModel
import org.junit.Assert.assertEquals
import org.junit.Test

class RandomRepositoryTest {
    @Test
    fun refreshReplacesCurrentList() {
        val source = FakeRandomDataSource(
            items = listOf(document("/scp-002", "SCP-002"))
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
            items = listOf(
                document("/scp-003", "SCP-003"),
                document("/scp-004", "SCP-004")
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

    private fun document(link: String, title: String): ScpItemModel {
        return ScpItemModel().apply {
            this.link = link
            this.title = title
        }
    }
}

private class FakeRandomDataSource(
    private val items: List<ScpItemModel>
) : RandomDataSource {
    val requestedRanges = mutableListOf<String>()

    override fun getRandomList(range: String): List<ScpItemModel> {
        requestedRanges.add(range)
        return items
    }
}
