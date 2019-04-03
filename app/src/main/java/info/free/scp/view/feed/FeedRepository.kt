package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.FeedModel
import info.free.scp.service.HttpManager

class FeedRepository {
    var feedList = MutableLiveData<ArrayList<FeedModel>>()

    fun loadFeedList(feedType: Int) {
//        feedList = articleDao.loadAll()
        HttpManager.instance.getLatest(feedType) {
            feedList.value = it as ArrayList<FeedModel>
        }
    }
}