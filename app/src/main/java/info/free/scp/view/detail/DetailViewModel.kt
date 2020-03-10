package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.ONLINE
import info.free.scp.bean.ScpCollectionModel
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.PreferenceUtil
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    var repo = DetailRepository()

    fun getScp(): MutableLiveData<in ScpModel>? {
        return repo.scp
    }

    fun getOfflineScp(): LiveData<ScpItemModel>? {
        return repo.offlineScp
    }

    fun getOfflineCollection(): LiveData<ScpCollectionModel>? {
        return repo.offlineCollection
    }

    fun getScpLikeInfo(): LiveData<ScpLikeModel>? {
        return repo.scpLikeInfo
    }

    fun getDetail(): MutableLiveData<String?> {
        return repo.detail
    }

    fun getOfflineDetail(): LiveData<String> {
        return repo.offlineDetail
    }

    fun likeScp(scpInfo: ScpLikeModel) {
        AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
    }

    /**
     * 变更scp时就顺带更新其他信息
     */
    fun setScp(link: String, title: String) {
        repo.setScp(link, title)
    }

    fun setScpReadInfo() {
        repo.setScpReadInfo()
    }

    fun setScpLikeInfo() {
        repo.setScpLikeInfo()
    }

    fun loadDetail(link: String) {
        if (PreferenceUtil.getAppMode() == SCPConstants.AppMode.OFFLINE) {
            repo.loadOfflineDetail(link)
        } else {
            viewModelScope.launch {
                repo.loadDetail(link)
            }
        }
    }

    fun loadComment(link: String) {
        viewModelScope.launch {
            repo.loadComment(link)
        }
    }
}