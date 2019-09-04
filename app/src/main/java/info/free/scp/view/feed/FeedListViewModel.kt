package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.FeedModel
import kotlinx.coroutines.launch

class FeedListViewModel : ViewModel() {
    private val feedRepo = FeedRepository()
    fun getFeed(): MutableLiveData<List<FeedModel>>? {
        return feedRepo.feedList
    }

    fun loadFeed(feedType: Int) {
        viewModelScope.launch {
            feedRepo.loadFeedList(feedType)
        }
    }
}
