package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import info.free.scp.bean.FeedModel

class FeedListViewModel(private val feedRepo: FeedRepository) : ViewModel() {
    fun getFeed(feedType: Int): MutableLiveData<ArrayList<FeedModel>>? {
        loadFeed(feedType)
        return feedRepo.feedList
    }

    fun loadFeed(feedType: Int) {
        feedRepo.loadFeedList(feedType)
    }
}
