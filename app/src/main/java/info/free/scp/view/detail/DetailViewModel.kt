package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

data class DetailUiState(
    val document: ScpModel? = null,
    val content: String? = null,
    val tag: String? = null,
    val comments: List<CommentModel>? = null,
    val isOfflineReadingEnabled: Boolean = false
)

class DetailViewModel(
    private val detailSettings: DetailSettingsStore = PreferenceDetailSettingsStore()
) : ViewModel() {
    private val repo = DetailRepository()
    private val readingListRepository = ReadingListRepository()
    private val _uiState = MediatorLiveData(
        DetailUiState(isOfflineReadingEnabled = detailSettings.isOfflineReadingEnabled)
    )
    val uiState: LiveData<DetailUiState> = _uiState
    private var offlineDetailSource: LiveData<String>? = null
    private var offlineTagSource: LiveData<String>? = null

    init {
        _uiState.addSource(repo.scp) { document -> updateState { copy(document = document) } }
        _uiState.addSource(repo.commentList) { comments -> updateState { copy(comments = comments) } }
        if (!detailSettings.isOfflineReadingEnabled) {
            _uiState.addSource(repo.detail) { content -> updateState { copy(content = content) } }
            _uiState.addSource(repo.tag) { tag -> updateState { copy(tag = tag) } }
        }
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
            bindOfflineSources()
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

    private fun bindOfflineSources() {
        offlineDetailSource?.let(_uiState::removeSource)
        offlineTagSource?.let(_uiState::removeSource)
        offlineDetailSource = repo.offlineDetail.also { source ->
            _uiState.addSource(source) { content -> updateState { copy(content = content) } }
        }
        offlineTagSource = repo.offlineTag.also { source ->
            _uiState.addSource(source) { tag -> updateState { copy(tag = tag) } }
        }
    }

    private fun updateState(transform: DetailUiState.() -> DetailUiState) {
        _uiState.value = (_uiState.value ?: DetailUiState()).transform()
    }
}
