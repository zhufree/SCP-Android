package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.PreferenceUtil
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    var repo = DetailRepository()

    fun getScp(): MutableLiveData<ScpItemModel?> {
        return repo.scp
    }

    fun getScpLikeInfo(): LiveData<ScpLikeModel>? {
        return repo.scpLikeInfo
    }

    fun getDetail(): MutableLiveData<String?> {
        return repo.detail
    }

    fun getTag(): MutableLiveData<String?> {
        return repo.tag
    }

    fun getOfflineDetail(): LiveData<String> {
        return repo.offlineDetail
    }

    fun getOfflineTag(): LiveData<String> {
        return repo.offlineTag
    }

    fun likeScp(scpInfo: ScpLikeModel) {
        AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
    }

    /**
     * 变更scp时就顺带更新其他信息
     */
    fun setScp(link: String) {
        repo.setScp(link)
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
            repo.loadOfflineTag(link)
        } else {
            viewModelScope.launch {
                repo.loadDetail(link)
                repo.loadTag(link)
            }
        }
    }

    fun loadComment(link: String) {
        viewModelScope.launch {
            repo.loadComment(link)
        }
    }
}