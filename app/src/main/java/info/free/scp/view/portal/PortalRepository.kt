package info.free.scp.view.portal

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.bean.PortalModel
import info.free.scp.service.FreeService
import toast


class PortalRepository {
    var portalList: MutableLiveData<List<PortalModel>> = MutableLiveData()
//    private var portalDao = AppInfoDatabase.getInstance().portalDao()
//    var portalList = portalDao.getAllDraft()

    suspend fun loadPortalList(): MutableLiveData<List<PortalModel>> {

        val response = apiCall { FreeService.getAllPortal() }
        response?.let {
            executeResponse(response, {
                toast("加载失败")
            }, {
                portalList.postValue(response.results)
            })
        }
        return portalList
    }
}