package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpItemModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.service.HttpManager
import info.free.scp.util.PreferenceUtil
import info.free.scp.SCPConstants.AppMode.OFFLINE

class RandomRepository {
    var randomList = MutableLiveData<List<ScpItemModel>>()

    fun getRandomList(range: String, add: Boolean = false) {
        if (add) {
            randomList.postValue(
                randomList.value?.plus(
                    ScpDataHelper.getInstance().getRandomScpList(range)
                )
            )
        } else {
            randomList.postValue(ScpDataHelper.getInstance().getRandomScpList(range))
        }
    }
}