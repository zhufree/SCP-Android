package info.free.scp.data.direct

import apiCall
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.bean.ScpModel
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.service.HttpManager
import info.free.scp.util.PreferenceUtil

sealed interface DirectLookupResult {
    data class Found(
        val document: ScpModel,
        val includeTitleInNavigation: Boolean
    ) : DirectLookupResult
    data object NotFound : DirectLookupResult
    data object Failed : DirectLookupResult
}

interface DirectDocumentDataSource {
    fun isOfflineReadingEnabled(): Boolean
    fun findLocalDocument(type: Int, numberPattern: String): ScpModel?
    suspend fun findRemoteDocument(type: Int, numberPattern: String): DirectLookupResult
}

class DefaultDirectDocumentDataSource(
    private val catalogRepository: ScpCatalogRepository = ScpCatalogRepository()
) : DirectDocumentDataSource {
    override fun isOfflineReadingEnabled(): Boolean {
        return PreferenceUtil.getAppMode() == OFFLINE
    }

    override fun findLocalDocument(type: Int, numberPattern: String): ScpModel? {
        return catalogRepository.getDocumentByNumber(type, numberPattern)
    }

    override suspend fun findRemoteDocument(
        type: Int,
        numberPattern: String
    ): DirectLookupResult {
        val response = apiCall {
            HttpManager.instance.getDirect(type, numberPattern)
        } ?: return DirectLookupResult.Failed
        if (!response.error.isNullOrEmpty()) return DirectLookupResult.Failed
        val document = response.results.firstOrNull()
            ?: return DirectLookupResult.NotFound
        return DirectLookupResult.Found(document, includeTitleInNavigation = true)
    }
}

class DirectDocumentRepository(
    private val dataSource: DirectDocumentDataSource = DefaultDirectDocumentDataSource()
) {
    suspend fun findDocument(type: Int, numberPattern: String): DirectLookupResult {
        if (!dataSource.isOfflineReadingEnabled()) {
            return dataSource.findRemoteDocument(type, numberPattern)
        }
        val document = dataSource.findLocalDocument(type, numberPattern)
            ?: return DirectLookupResult.NotFound
        return DirectLookupResult.Found(document, includeTitleInNavigation = false)
    }
}
