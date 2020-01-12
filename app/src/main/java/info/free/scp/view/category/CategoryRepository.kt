package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.bean.ScpModel
import info.free.scp.service.HttpManager

class CategoryRepository {
    var scpList = MutableLiveData<List<ScpModel>>()

    suspend fun loadCatList(scpType: Int) {
        val response = apiCall { HttpManager.instance.getCategory(scpType) }
        response?.let {
            executeResponse(response, {

            }, {
                scpList.value = response.results
                // TODO 加上阅读信息
            })
        }
    }

    fun reverseCat() {
        val innerList = scpList.value?.toMutableList()
        innerList?.reverse()
        scpList.postValue(innerList)
    }
}