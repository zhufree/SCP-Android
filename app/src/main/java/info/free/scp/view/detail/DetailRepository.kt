package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.bean.*
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.toast
import toast


class DetailRepository {
    var scp: MutableLiveData<ScpItemModel?> = MutableLiveData()
    var scpLikeInfo: LiveData<ScpLikeModel>? = null
    var detail: MutableLiveData<String?> = MutableLiveData("")
    var commentList: MutableLiveData<List<CommentModel>> = MutableLiveData()
    var offlineDetail: LiveData<String> = MutableLiveData()


    private var scpDao = ScpDatabase.getInstance().scpDao()
    private var likeDao = AppInfoDatabase.getInstance().likeAndReadDao()

    fun setScp(link: String) {
        scp.postValue(scpDao.getScpByLink(link))
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
            }
        }
    }

    fun loadOfflineDetail(link: String) {
        offlineDetail = DetailDatabase.getInstance()?.detailDao()?.getLiveDetail(link)
                ?: MutableLiveData()
    }

    suspend fun loadDetail(link: String) {
        val response = apiCall { HttpManager.instance.getDetail(link.substring(1)) }
        response?.let {
            executeResponse(response, {

            }, {
                if (response.results.isNotEmpty()) {
                    detail.postValue(response.results[0])
                }
            })
        }
    }

    suspend fun loadComment(link: String) {
        val response = apiCall { HttpManager.instance.getComment(link.substring(1)) }
        response?.let {
            executeResponse(response, {

            }, {
                info(response.results.toString())
                commentList.postValue(response.results)
            })
        }
    }
}