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
import info.free.scp.data.detail.DetailReadResult
import info.free.scp.data.reading.ReadingDocumentStatus
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.data.settings.DetailSettingsStore
import info.free.scp.data.settings.PreferenceDetailSettingsStore
import info.free.scp.util.normalizeLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed interface ArticleBody {
    data object Loading : ArticleBody
    data object Website : ArticleBody
    data class Content(val html: String, val tags: String?) : ArticleBody
    data class Message(val text: String) : ArticleBody
}

internal data class DetailUiState(
    val articleKey: String = "",
    val requestedLink: String = "",
    val document: ScpModel? = null,
    val body: ArticleBody = ArticleBody.Loading,
    val comments: List<CommentModel>? = null
)

class DetailViewModel(
    private val detailSettings: DetailSettingsStore = PreferenceDetailSettingsStore()
) : ViewModel() {
    private val repo = DetailRepository()
    private val readingListRepository = ReadingListRepository()
    private val _uiState = MutableLiveData(DetailUiState())
    internal val uiState: LiveData<DetailUiState> = _uiState
    private var articleJob: Job? = null
    private var commentJob: Job? = null

    fun setScp(link: String, knownDocument: ScpModel? = null) {
        val key = normalizeLink(link)
        if (_uiState.value?.articleKey == key) return
        articleJob?.cancel()
        commentJob?.cancel()
        _uiState.value = DetailUiState(articleKey = key, requestedLink = link, document = knownDocument)
        articleJob = viewModelScope.launch {
            val document = knownDocument ?: withContext(Dispatchers.IO) {
                runCatching { repo.getDocumentByLink(key) }.getOrNull()
            }
            updateState { copy(document = document) }
            val body = withContext(Dispatchers.IO) { readArticle(key) }
            updateState { copy(body = body) }
        }
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
        detailSettings.increasePoints(2)
        readingListRepository.setLiked(link, title, boxId)
    }

    fun setUnliked(link: String, title: String) {
        readingListRepository.setUnliked(link, title)
    }

    fun toggleRead(link: String, title: String): Boolean {
        val hasRead = readingListRepository.toggleRead(link, title)
        if (hasRead) {
            detailSettings.increasePoints(5)
        } else {
            detailSettings.decreasePoints(5)
        }
        return hasRead
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

    fun rewardReadModeSwitch() {
        detailSettings.increasePoints(1)
    }

    fun rewardExternalOpen() {
        detailSettings.increasePoints(1)
    }

    fun rewardArticleNavigation() {
        detailSettings.increasePoints(1)
    }

    private suspend fun readArticle(key: String): ArticleBody {
        val local = if (detailSettings.isOfflineReadingEnabled) repo.loadOfflineDetail(key)
            else DetailReadResult.Missing
        when (local) {
            is DetailReadResult.Hit -> return ArticleBody.Content(local.html, local.tags)
            DetailReadResult.NotFound -> return ArticleBody.Message("文章不存在（404）")
            DetailReadResult.Missing, DetailReadResult.Failed -> Unit
        }
        if (!repo.isNetworkAvailable()) {
            return ArticleBody.Message(when {
                local == DetailReadResult.Failed -> "本地正文读取失败，请联网后重试"
                detailSettings.isOfflineReadingEnabled -> "本地未下载这篇正文，请联网阅读或安装对应正文块"
                else -> "当前无网络，请联网后阅读"
            })
        }
        return when (val online = repo.loadOnlineDetail(key)) {
            is DetailReadResult.Hit -> ArticleBody.Content(online.html, online.tags)
            DetailReadResult.NotFound -> ArticleBody.Message("文章不存在（404）")
            DetailReadResult.Missing, DetailReadResult.Failed -> ArticleBody.Website
        }
    }

    fun loadComment(link: String) {
        val key = normalizeLink(link)
        commentJob?.cancel()
        commentJob = viewModelScope.launch {
            val comments = repo.loadComment(key)
            val current = _uiState.value?.articleKey
            if (current == key || current.isNullOrEmpty()) updateState { copy(comments = comments) }
        }
    }

    private fun updateState(transform: DetailUiState.() -> DetailUiState) {
        _uiState.value = (_uiState.value ?: DetailUiState()).transform()
    }
}
