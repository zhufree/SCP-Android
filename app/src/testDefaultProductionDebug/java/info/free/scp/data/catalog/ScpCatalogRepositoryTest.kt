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

    @Test
    fun documentLookupReturnsCatalogEntryByLink() {
        val source = FakeScpCatalogDataSource().apply {
            documentsByLink["/scp-013"] = document("/scp-013", "SCP-013")
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.getDocumentByLink("/scp-013")

        assertEquals("/scp-013", result?.link)
    }

    @Test
    fun documentLookupReturnsCatalogEntryByTypeAndNumberPattern() {
        val source = FakeScpCatalogDataSource().apply {
            documentsByNumber[SAVE_SERIES to "%-173 %"] = document("/scp-173", "SCP-173")
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.getDocumentByNumber(SAVE_SERIES, "%-173 %")

        assertEquals("/scp-173", result?.link)
    }

    @Test
    fun nextDocumentUsesCurrentIndexAndType() {
        val source = FakeScpCatalogDataSource().apply {
            nextDocuments[13 to SAVE_SERIES] = document("/scp-014", "SCP-014")
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.getNextDocument(index = 13, type = SAVE_SERIES)

        assertEquals("/scp-014", result?.link)
    }

    @Test
    fun previousDocumentUsesCurrentIndexAndType() {
        val source = FakeScpCatalogDataSource().apply {
            previousDocuments[13 to SAVE_SERIES] = document("/scp-012", "SCP-012")
        }
        val repository = ScpCatalogRepository(source)

        val result = repository.getPreviousDocument(index = 13, type = SAVE_SERIES)

        assertEquals("/scp-012", result?.link)
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
    val documentsByNumber = mutableMapOf<Pair<Int, String>, ScpModel>()
    val nextDocuments = mutableMapOf<Pair<Int, Int>, ScpModel>()
    val previousDocuments = mutableMapOf<Pair<Int, Int>, ScpModel>()
    var hideFinished = false
    var readLinkSet = emptySet<String>()
    var detailDataAvailable = true

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

    override fun getDocumentByNumber(type: Int, numberPattern: String): ScpModel? {
        return documentsByNumber[type to numberPattern]
    }

    override fun getNextDocument(index: Int, type: Int): ScpModel? {
        return nextDocuments[index to type]
    }

    override fun getPreviousDocument(index: Int, type: Int): ScpModel? {
        return previousDocuments[index to type]
    }

    override fun getReadLinks(): Set<String> {
        return readLinkSet
    }

    override fun shouldHideFinished(): Boolean {
        return hideFinished
    }

    override fun isDetailDataAvailable(): Boolean {
        return detailDataAvailable
    }
}
