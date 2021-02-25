package info.free.scp.view.later

import androidx.lifecycle.ViewModel
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.*
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper

class LaterViewModel : ViewModel() {
    val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    fun getLaterList(): List<ScpRecordModel> {
        return ScpDataHelper.getInstance().getViewListByTypeAndOrder(LATER_TYPE, ASC)
    }

    fun getLikeBoxList(): List<ScpLikeBox> {
        return likeDao.getLikeBox()
    }

    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel> {
        return likeDao.getLikeListByBoxId(boxId)
    }
}