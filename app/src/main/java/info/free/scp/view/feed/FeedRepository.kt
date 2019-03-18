package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import info.free.scp.bean.FeedModel
import info.free.scp.service.HttpManager

class FeedRepository {
    var feedList = MutableLiveData<ArrayList<FeedModel>>()

    fun loadFeedList() {
//        feedList = articleDao.loadAll()
        HttpManager.instance.getLatestCn {
            feedList?.value = it as ArrayList<FeedModel>
        }
    }
}