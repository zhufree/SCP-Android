package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.FeedModel
import info.free.scp.data.feed.FeedIndexUiState
import info.free.scp.data.feed.FeedRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedListViewModel : ViewModel() {
    private val feedRepo = FeedRepository()
    private val mutableFeedIndexState = MutableStateFlow(FeedIndexUiState())
    private var feedIndexJob: Job? = null

    /** 首页最近原创/翻译：区分加载中、有内容、空、失败。 */
    val feedIndexState: StateFlow<FeedIndexUiState> = mutableFeedIndexState.asStateFlow()

    fun getFeed(): MutableLiveData<List<FeedModel>> {
        return feedRepo.feedList
    }

    fun loadFeed(feedType: Int) {
        viewModelScope.launch {
            feedRepo.loadFeedList(feedType)
        }
    }

    /**
     * 请求首页 Feed。已有内容且未强制刷新时不重复请求；重试传 [force] = true。
     * 上一次请求还没结束时直接忽略，避免并发覆盖。
     */
    fun loadFeedIndex(force: Boolean = false) {
        if (feedIndexJob?.isActive == true) return
        if (!force && !mutableFeedIndexState.value.needsLoad) return
        mutableFeedIndexState.update { it.beginLoad() }
        feedIndexJob = viewModelScope.launch {
            val result = feedRepo.fetchFeedIndex()
            mutableFeedIndexState.update { it.applyResult(result) }
        }
    }
}
