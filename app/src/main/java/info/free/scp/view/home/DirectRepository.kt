package info.free.scp.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.bean.*
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager
import info.free.scp.util.PreferenceUtil


class DirectRepository {
    var scp: MutableLiveData<in ScpModel> = MutableLiveData()
    var offlineScp: LiveData<ScpItemModel>? = null
    var offlineCollection: LiveData<ScpCollectionModel>? = null


    private var scpDao = ScpDatabase.getInstance()?.scpDao()


//    suspend fun loadDetail(link: String) {
//        val response = apiCall { HttpManager.instance.getDetail(link.substring(1)) }
//        response?.let {
//            executeResponse(response, {
//
//            }, {
//                if (response.results.isNotEmpty()) {
//                    detail.postValue(response.results[0])
//                }
//            })
//        }
//    }
//
//    suspend fun loadComment(link: String) {
//        val response = apiCall { HttpManager.instance.getComment(link.substring(1)) }
//        response?.let {
//            executeResponse(response, {
//
//            }, {
//                if (response.results.isNotEmpty()) {
//                    info(response.results.toString())
//                    commentList.postValue(response.results)
//                }
//            })
//        }
//    }


}