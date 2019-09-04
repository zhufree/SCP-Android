package info.free.scp.view.feed

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.bean.FeedModel
import info.free.scp.service.HttpManager

class FeedRepository {
    var feedList = MutableLiveData<List<FeedModel>>()

    suspend fun loadFeedList(feedType: Int) {
        val response = apiCall { HttpManager.instance.getLatest(feedType) }
        response?.let {
            executeResponse(response, {

            }, {
                feedList.value = response.results
            })
        }
    }
}