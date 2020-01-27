package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.bean.ScpModel
import info.free.scp.service.HttpManager

class CategoryRepository {
    var scpList = MutableLiveData<List<ScpModel>>()

    suspend fun loadCatList(scpType: Int, subScpType: String, limit: Int, start: Int) {
        val response = apiCall { HttpManager.instance.getCategory(scpType, subScpType, limit, start) }
        response?.let {
            executeResponse(response, {

            }, {
                scpList.value = response.results
            })
        }
    }

    fun reverseCat() {
        val innerList = scpList.value?.toMutableList()
        innerList?.reverse()
        scpList.postValue(innerList)
    }
}