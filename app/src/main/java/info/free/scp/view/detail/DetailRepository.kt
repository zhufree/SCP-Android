package info.free.scp.view.detail

import androidx.lifecycle.LiveData
import info
import info.free.scp.SCPConstants
import info.free.scp.bean.DraftModel
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DetailRepository {
    var scp: LiveData<out ScpModel>? = null
    var scpLikeInfo: LiveData<ScpLikeModel>? = null
    private var scpDao = ScpDatabase.getInstance()?.scpDao()
    private var likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    var scpLikeBoxList: LiveData<List<ScpLikeBox>> = likeDao.getLiveLikeBox()

    fun setScp(link: String) {
        scp = scpDao?.getLiveScpByLink(link) ?: scpDao?.getLiveCollectionByLink(link)
        scp?.value?.let {
            ScpDataHelper.getInstance().insertViewListItem(it.link, it.title, SCPConstants.HISTORY_TYPE)
            AppInfoDatabase.getInstance().readRecordDao().delete(it.link, SCPConstants.LATER_TYPE)
        }
    }

    fun setScpLikeInfo() {
        scp?.value?.let {
            scpLikeInfo = likeDao.getLiveInfoByLink(it.link)
            if (scpLikeInfo?.value == null) {
                val likeInfo = ScpLikeModel(it.link, it.title, false, hasRead = false, boxId = 0)
                likeDao.save(likeInfo)
                scpLikeInfo = likeDao.getLiveInfoByLink(it.link)
                info("create new like info:${scpLikeInfo?.value?.title ?: ""}")
            }
        }
    }

    fun setScpBoxList() {
        scpLikeBoxList.value?.let {
            if (it.isEmpty()) {
                likeDao.addLikeBox(ScpLikeBox(0, "默认收藏夹"))
            }
        }
        scpLikeBoxList = likeDao.getLiveLikeBox()
    }
}