package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager
import info.free.scp.util.PreferenceUtil


class DetailRepository {
    var scp: MutableLiveData<in ScpModel> = MutableLiveData()
    var offlineScp: LiveData<ScpItemModel>? = null
    var offlineCollection: LiveData<ScpCollectionModel>? = null
    var scpLikeInfo: LiveData<ScpLikeModel>? = null
    var detail: MutableLiveData<String?> = MutableLiveData("")
    var offlineDetail: LiveData<String> = MutableLiveData()
    private var scpDao = ScpDatabase.getInstance()?.scpDao()
    private var likeDao = AppInfoDatabase.getInstance().likeAndReadDao()

    fun setScp(link: String, title: String) {
        if (PreferenceUtil.getAppMode() == OFFLINE) {
            offlineScp = scpDao?.getLiveScpByLink(link)
            offlineCollection = scpDao?.getLiveCollectionByLink(link)
        } else {
            val tempScp = ScpItemModel("", "")
            tempScp.title = title
            tempScp.link = link
            scp.postValue(tempScp)
        }
    }

    fun setScpReadInfo() {
        scp.value?.let {
            it as ScpModel
            ScpDataHelper.getInstance().insertViewListItem(it.link, it.title, SCPConstants.HISTORY_TYPE)
            AppInfoDatabase.getInstance().readRecordDao().delete(it.link, SCPConstants.LATER_TYPE)
            scpLikeInfo = likeDao.getLiveInfoByLink(it.link)
        }
    }

    fun setScpLikeInfo() {
        scp.value?.let {
            it as ScpModel
            if (scpLikeInfo?.value == null) {
                val likeInfo = ScpLikeModel(it.link, it.title, false, hasRead = false, boxId = 0)
                info("save new like info:${likeInfo}")
                likeDao.save(likeInfo)
//                scpLikeInfo = likeDao.getLiveInfoByLink(it.link)
            }
        }
    }

    fun loadOfflineDetail(link: String) {
        offlineDetail = ScpDatabase.getInstance()?.detailDao()?.getLiveDetail(link)
                ?: MutableLiveData()
        offlineDetail
    }

    suspend fun loadDetail(link: String) {
//        if (PreferenceUtil.getAppMode() == OFFLINE) {
//            offlineDetail = ScpDatabase.getInstance()?.detailDao()?.getLiveDetail(link)
////            detail.postValue(ScpDatabase.getInstance()?.detailDao()?.getLiveDetail(link)?.value
////                    ?: "")
//        } else {
        val response = apiCall { HttpManager.instance.getDetail(link.substring(1)) }
        response?.let {
            executeResponse(response, {

            }, {
                if (response.results.isNotEmpty()) {
                    detail.postValue(response.results[0])
                }
            })
        }
//        }
    }
}