package info.free.scp.view.later

import androidx.lifecycle.ViewModel
import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.*
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDataHelper

class LaterViewModel : ViewModel() {
    private val likeDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val recordDao = AppInfoDatabase.getInstance().readRecordDao()

    fun getRecordList(type: Int, order: Int): List<ScpRecordModel> {
        val resultList = emptyList<ScpRecordModel>().toMutableList()
        resultList.addAll(if (order == ASC) recordDao.getInfoByLinkAsc(type)
        else recordDao.getInfoByLinkDesc(type))
        return resultList
    }


    fun deleteRecord(record: ScpRecordModel) {
        recordDao.delete(record)
    }

    fun getLikeBoxList(): List<ScpLikeBox> {
        val boxList = arrayListOf<ScpLikeBox>()
        boxList.addAll(likeDao.getLikeBox())
        if (boxList.isEmpty()) {
            val defaultBox = ScpLikeBox(0, "默认收藏夹")
            boxList.add(defaultBox)
            likeDao.saveLikeBox(defaultBox)
        }
        return boxList
    }

    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel> {
        return likeDao.getLikeListByBoxId(boxId)
    }
}