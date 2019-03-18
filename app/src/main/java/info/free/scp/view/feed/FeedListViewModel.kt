package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import info.free.scp.bean.FeedModel

class FeedListViewModel(private val feedRepo: FeedRepository) : ViewModel() {
    fun getFeed(): MutableLiveData<ArrayList<FeedModel>>? {
        loadFeed()
        return feedRepo.feedList
    }

    fun loadFeed() {
        feedRepo.loadFeedList()
    }
}
