package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.bean.CommentModel
import info.free.scp.data.detail.DetailRepository
import info.free.scp.data.reading.ReadingDocumentStatus
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.data.settings.DetailSettingsStore
import info.free.scp.data.settings.PreferenceDetailSettingsStore
import kotlinx.coroutines.launch

class DetailViewModel(
    private val detailSettings: DetailSettingsStore = PreferenceDetailSettingsStore()
) : ViewModel() {
    private val repo = DetailRepository()
    private val readingListRepository = ReadingListRepository()

    fun getScp(): MutableLiveData<ScpModel?> {
        return repo.scp
    }

    fun getDetail(): MutableLiveData<String?> {
        return repo.detail
    }

    fun getTag(): MutableLiveData<String?> {
        return repo.tag
    }

    fun getOfflineDetail(): LiveData<String> {
        return repo.offlineDetail
    }

    fun getOfflineTag(): LiveData<String> {
        return repo.offlineTag
    }

    fun getCommentList(): LiveData<List<CommentModel>> {
        return repo.commentList
    }

    /**
     * 变更scp时就顺带更新其他信息
     */
    fun setScp(link: String) {
        repo.setScp(link)
    }

    fun getDocumentByLink(link: String): ScpModel? {
        return repo.getDocumentByLink(link)
    }

    fun getNextDocument(index: Int, type: Int): ScpModel? {
        return repo.getNextDocument(index, type)
    }

    fun getPreviousDocument(index: Int, type: Int): ScpModel? {
        return repo.getPreviousDocument(index, type)
    }

    fun recordOpened(link: String, title: String) {
        readingListRepository.recordOpened(link, title)
    }

    fun getDocumentStatus(link: String): ReadingDocumentStatus {
        return readingListRepository.getDocumentStatus(link)
    }

    fun getLikeInfo(link: String, title: String): ScpLikeModel {
        return readingListRepository.getLikeInfo(link, title)
    }

    fun setLiked(link: String, title: String, boxId: Int) {
        readingListRepository.setLiked(link, title, boxId)
    }

    fun setUnliked(link: String, title: String) {
        readingListRepository.setUnliked(link, title)
    }

    fun toggleRead(link: String, title: String): Boolean {
        return readingListRepository.toggleRead(link, title)
    }

    fun addToLater(link: String, title: String) {
        readingListRepository.addToLater(link, title)
    }

    fun getLikeBoxList(): List<ScpLikeBox> {
        return readingListRepository.getLikeBoxList()
    }

    fun createLikeBox(name: String) {
        readingListRepository.createLikeBox(name)
    }

    fun loadDetail(link: String) {
        if (detailSettings.isOfflineReadingEnabled) {
            repo.loadOfflineDetail(link)
            repo.loadOfflineTag(link)
        } else {
            viewModelScope.launch {
                repo.loadDetail(link)
                repo.loadTag(link)
            }
        }
    }

    fun loadComment(link: String) {
        viewModelScope.launch {
            repo.loadComment(link)
        }
    }
}
