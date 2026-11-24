package info.free.scp.data.detail

import info.free.scp.ScpApplication
import info.free.scp.bean.CommentModel
import info.free.scp.bean.ScpModel
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.db.DetailDatabase
import info.free.scp.service.HttpManager
import info.free.scp.util.Utils
import info.free.scp.util.normalizeLink
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

internal class DetailRepository {
    private val catalogRepository = ScpCatalogRepository()

    fun getDocumentByLink(link: String): ScpModel? = catalogRepository.getDocumentByLink(link)

    fun getNextDocument(index: Int, type: Int): ScpModel? = catalogRepository.getNextDocument(index, type)

    fun getPreviousDocument(index: Int, type: Int): ScpModel? = catalogRepository.getPreviousDocument(index, type)

    fun loadOfflineDetail(link: String): DetailReadResult {
        return try {
            val dao = DetailDatabase.getInstance()?.detailDao() ?: return DetailReadResult.Missing
            val key = normalizeLink(link)
            // Newly published parts use canonical keys. Prefer those over unowned legacy aliases.
            classifyDetail(dao.getArticle(key) ?: dao.getLegacyArticle(key))
        } catch (failure: Exception) {
            classifyDetail(null, failure)
        }
    }

    fun isNetworkAvailable(): Boolean = Utils.enabledNetwork(ScpApplication.context)

    suspend fun loadOnlineDetail(link: String): DetailReadResult {
        val path = DetailLinkResolver.toApiPath(link)
        return try {
            val response = HttpManager.instance.getDetail(path)
            when {
                response.code == 404 -> DetailReadResult.NotFound
                !response.error.isNullOrEmpty() -> DetailReadResult.Failed
                else -> {
                    val html = response.results.firstOrNull()
                    if (html.isNullOrBlank() || html.trim() == "null") return DetailReadResult.Missing
                    val tags = loadOnlineTags(path)
                    DetailReadResult.Hit(html, tags)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: HttpException) {
            if (failure.code() == 404) DetailReadResult.NotFound else DetailReadResult.Failed
        } catch (failure: Exception) {
            DetailReadResult.Failed
        }
    }

    private suspend fun loadOnlineTags(path: String): String? = try {
        HttpManager.instance.getTag(path).results.firstOrNull()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        null
    }

    suspend fun loadComment(link: String): List<CommentModel> = try {
        HttpManager.instance.getComment(DetailLinkResolver.toApiPath(link)).results
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        emptyList()
    }
}
