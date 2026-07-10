package info.free.scp.data.catalog

import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.ScpType.SAVE_ANOMALOUS_CN
import info.free.scp.SCPConstants.ScpType.SAVE_LIBRARY_PAGE
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SHORT_STORY
import info.free.scp.bean.ScpModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ScpCatalogRepositoryTest {
    @Test
    fun libraryDocsCombineConfiguredSourceTypesInOrder() {
        val source = FakeScpCatalogDataSource().apply {
            documentsByType[SAVE_LIBRARY_PAGE] = listOf(document("/library", "Library"))
            documentsByType[SAVE_SHORT_STORY] = listOf(document("/short", "Short Story"))
            documentsByType[SAVE_ANOMALOUS_CN] = listOf(document("/anomalous", "Anomalous"))
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.getDocList(SCPConstants.Entry.LIBRARY_DOC)

        assertEquals(listOf("/library", "/short", "/anomalous"), result.map { it.link })
    }

    @Test
    fun seriesPaginationReturnsCompleteHundredItemWindow() {
        val source = FakeScpCatalogDataSource().apply {
            documentsByType[SAVE_SERIES] = (1..105).map { number ->
                document("/scp-$number", "SCP-$number")
            }
        }
        val repository = ScpCatalogRepository(source)

        val firstPage = repository.getDocList(SAVE_SERIES, groupIndex = 0)
        val secondPage = repository.getDocList(SAVE_SERIES, groupIndex = 1)

        assertEquals(100, firstPage.size)
        assertEquals((101..105).map { "/scp-$it" }, secondPage.map { it.link })
    }

    @Test
    fun hideFinishedFiltersReadDocumentsAtQueryTime() {
        val source = FakeScpCatalogDataSource().apply {
            documentsByType[SAVE_SERIES] = listOf(
                document("/scp-001", "SCP-001"),
                document("/scp-002", "SCP-002")
            )
            hideFinished = true
            readLinkSet = setOf("/scp-001")
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.getDocList(SAVE_SERIES)

        assertEquals(listOf("/scp-002"), result.map { it.link })
    }

    @Test
    fun titleSearchDelegatesToCatalogSource() {
        val source = FakeScpCatalogDataSource().apply {
            titleSearchResults["%clock%"] = listOf(document("/scp-914", "Clockworks"))
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.searchTitle("%clock%")

        assertEquals(listOf("/scp-914"), result.map { it.link })
    }

    @Test
    fun detailSearchPreservesHitOrderAndSkipsMissingCatalogRows() {
        val source = FakeScpCatalogDataSource().apply {
            detailSearchResults["%memory%"] = listOf("/scp-002", "/missing", "/scp-001")
            documentsByLink["/scp-001"] = document("/scp-001", "SCP-001")
            documentsByLink["/scp-002"] = document("/scp-002", "SCP-002")
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.searchDetail("%memory%")

        assertEquals(listOf("/scp-002", "/scp-001"), result.map { it.link })
    }

    private fun document(link: String, title: String): ScpModel {
        return ScpModel(link = link, title = title)
    }
}

private class FakeScpCatalogDataSource : ScpCatalogDataSource {
    val documentsByType = mutableMapOf<Int, List<ScpModel>>()
    val talesByTypeAndSubType = mutableMapOf<Pair<Int, String>, List<ScpModel>>()
    val internationalByCountry = mutableMapOf<String, List<ScpModel>>()
    val titleSearchResults = mutableMapOf<String, List<ScpModel>>()
    val detailSearchResults = mutableMapOf<String, List<String>>()
    val documentsByLink = mutableMapOf<String, ScpModel>()
    var hideFinished = false
    var readLinkSet = emptySet<String>()

    override fun getDocumentsByType(type: Int): List<ScpModel> {
        return documentsByType[type].orEmpty()
    }

    override fun getTalesByTypeAndSubType(type: Int, subType: String): List<ScpModel> {
        return talesByTypeAndSubType[type to subType].orEmpty()
    }

    override fun getInternationalByCountry(countryPrefix: String): List<ScpModel> {
        return internationalByCountry[countryPrefix].orEmpty()
    }

    override fun searchByTitle(keyword: String): List<ScpModel> {
        return titleSearchResults[keyword].orEmpty()
    }

    override fun searchDetailLinks(keyword: String): List<String> {
        return detailSearchResults[keyword].orEmpty()
    }

    override fun getDocumentByLink(link: String): ScpModel? {
        return documentsByLink[link]
    }

    override fun getReadLinks(): Set<String> {
        return readLinkSet
    }

    override fun shouldHideFinished(): Boolean {
        return hideFinished
    }
}
