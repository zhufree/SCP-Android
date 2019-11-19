package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.util.PreferenceUtil
import org.jetbrains.anko.selector

class DetailViewModel : ViewModel() {
    var repo = DetailRepository()

    fun getScp(): LiveData<out ScpModel>? {
        return repo.scp
    }

    fun getScpInfo(): LiveData<ScpLikeModel>? {
        return repo.scpLikeInfo
    }

    fun likeScp(scpInfo: ScpLikeModel) {
        AppInfoDatabase.getInstance().likeAndReadDao().save(scpInfo)
    }

    /**
     * 变更scp时就顺带更新其他信息
     */
    fun setScp(link: String) {
        repo.setScp(link)
        repo.setScpLikeInfo()
    }
}