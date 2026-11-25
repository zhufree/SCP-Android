package info.free.scp.data.feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.bean.FeedIndexModel
import info.free.scp.bean.FeedModel
import info.free.scp.service.HttpManager
import kotlin.coroutines.cancellation.CancellationException

class FeedRepository {
    var feedList = MutableLiveData<List<FeedModel>>()

    suspend fun loadFeedList(feedType: Int) {
        val response = apiCall { HttpManager.instance.getLatest(feedType) }
        response?.let {
            executeResponse(response, {}, {
                feedList.value = response.results
            })
        }
    }

    /** 首页用：失败不弹 Toast，交给调用方在区块内显示失败态并提供重试。 */
    suspend fun fetchFeedIndex(): Result<FeedIndexModel> = try {
        Result.success(HttpManager.instance.getLatestIndex())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.i("network", e.toString())
        Result.failure(e)
    }
}
