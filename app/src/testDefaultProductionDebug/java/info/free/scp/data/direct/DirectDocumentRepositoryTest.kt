package info.free.scp.data.direct

import info.free.scp.bean.ScpModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectDocumentRepositoryTest {
    @Test
    fun offlineModeUsesOnlyLocalCatalog() = runBlocking {
        val source = FakeDirectDocumentDataSource(
            offline = true,
            localDocument = ScpModel(link = "/scp-173", title = "SCP-173")
        )

        val result = DirectDocumentRepository(source).findDocument(1, "%-173 %")

        result as DirectLookupResult.Found
        assertEquals("/scp-173", result.document.link)
        assertFalse(result.includeTitleInNavigation)
        assertTrue(source.localLookupCalled)
        assertFalse(source.remoteLookupCalled)
    }

    @Test
    fun missingOfflineDocumentReturnsNotFound() = runBlocking {
        val source = FakeDirectDocumentDataSource(offline = true)

        val result = DirectDocumentRepository(source).findDocument(1, "%-999 %")

        assertEquals(DirectLookupResult.NotFound, result)
    }

    @Test
    fun onlineModeReturnsRemoteResultWithoutLocalLookup() = runBlocking {
        val source = FakeDirectDocumentDataSource(
            offline = false,
            remoteResult = DirectLookupResult.Found(
                ScpModel(link = "/scp-cn-173", title = "SCP-CN-173"),
                includeTitleInNavigation = true
            )
        )

        val result = DirectDocumentRepository(source).findDocument(2, "%-173 %")

        result as DirectLookupResult.Found
        assertEquals("/scp-cn-173", result.document.link)
        assertTrue(result.includeTitleInNavigation)
        assertFalse(source.localLookupCalled)
        assertTrue(source.remoteLookupCalled)
    }

    @Test
    fun onlineFailureIsPreserved() = runBlocking {
        val source = FakeDirectDocumentDataSource(
            offline = false,
            remoteResult = DirectLookupResult.Failed
        )

        val result = DirectDocumentRepository(source).findDocument(1, "%-173 %")

        assertEquals(DirectLookupResult.Failed, result)
    }
}

private class FakeDirectDocumentDataSource(
    private val offline: Boolean,
    private val localDocument: ScpModel? = null,
    private val remoteResult: DirectLookupResult = DirectLookupResult.NotFound
) : DirectDocumentDataSource {
    var localLookupCalled = false
    var remoteLookupCalled = false

    override fun isOfflineReadingEnabled(): Boolean = offline

    override fun findLocalDocument(type: Int, numberPattern: String): ScpModel? {
        localLookupCalled = true
        return localDocument
    }

    override suspend fun findRemoteDocument(
        type: Int,
        numberPattern: String
    ): DirectLookupResult {
        remoteLookupCalled = true
        return remoteResult
    }
}
