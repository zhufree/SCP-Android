package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info
import info.free.scp.bean.*
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.data.detail.DetailLinkResolver
import info.free.scp.db.DetailDatabase
import info.free.scp.service.HttpManager


class DetailRepository {
    var scp: MutableLiveData<ScpModel?> = MutableLiveData()
    var detail: MutableLiveData<String?> = MutableLiveData("")
    var tag: MutableLiveData<String?> = MutableLiveData("")
    var commentList: MutableLiveData<List<CommentModel>> = MutableLiveData()
    var offlineDetail: LiveData<String> = MutableLiveData()
    var offlineTag: LiveData<String> = MutableLiveData()
    private val catalogRepository = ScpCatalogRepository()

    fun setScp(link: String) {
        scp.postValue(catalogRepository.getDocumentByLink(link))
    }

    fun getDocumentByLink(link: String): ScpModel? {
        return catalogRepository.getDocumentByLink(link)
    }

    fun getNextDocument(index: Int, type: Int): ScpModel? {
        return catalogRepository.getNextDocument(index, type)
    }

    fun getPreviousDocument(index: Int, type: Int): ScpModel? {
        return catalogRepository.getPreviousDocument(index, type)
    }

    fun loadOfflineDetail(link: String) {
        offlineDetail = DetailDatabase.getInstance()?.detailDao()?.getLiveDetail(link)
                ?: MutableLiveData()
    }

    fun loadOfflineTag(link: String) {
        offlineTag = DetailDatabase.getInstance()?.detailDao()?.getLiveTag(link)
                ?: MutableLiveData()
    }

    suspend fun loadDetail(link: String) {
        val response = apiCall { HttpManager.instance.getDetail(DetailLinkResolver.toApiPath(link)) }
        response?.let {
            executeResponse(response, {

            }, {
                if (response.results.isNotEmpty()) {
                    detail.postValue(response.results[0])
                }
            })
        }
    }

    suspend fun loadTag(link: String) {
        val response = apiCall { HttpManager.instance.getTag(DetailLinkResolver.toApiPath(link)) }
        response?.let {
            executeResponse(response, {

            }, {
                if (response.results.isNotEmpty()) {
                    tag.postValue(response.results[0])
                }
            })
        }
    }

    suspend fun loadComment(link: String) {
        val response = apiCall { HttpManager.instance.getComment(DetailLinkResolver.toApiPath(link)) }
        response?.let {
            executeResponse(response, {
            }, {
                info(response.results.toString())
                commentList.postValue(response.results)
            })
        }
    }
}
