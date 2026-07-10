package info.free.scp.view.later

import androidx.lifecycle.ViewModel
import info.free.scp.bean.*
import info.free.scp.data.reading.ReadingListRepository

class LaterViewModel : ViewModel() {
    private val readingListRepository = ReadingListRepository()

    fun getRecordList(type: Int, order: Int): List<ScpRecordModel> {
        return readingListRepository.getRecordList(type, order)
    }


    fun deleteRecord(record: ScpRecordModel) {
        readingListRepository.deleteRecord(record)
    }

    fun clearHistory() {
        readingListRepository.clearHistory()
    }

    fun getLikeBoxList(): List<ScpLikeBox> {
        return readingListRepository.getLikeBoxList()
    }

    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel> {
        return readingListRepository.getLikeListByBoxId(boxId)
    }

    fun getExistingLikeBoxes(): List<ScpLikeBox> {
        return readingListRepository.getExistingLikeBoxes()
    }

    fun deleteLike(like: ScpLikeModel) {
        readingListRepository.deleteLike(like)
    }

    fun moveLike(like: ScpLikeModel, boxId: Int) {
        readingListRepository.moveLike(like, boxId)
    }

    fun deleteLikeBoxAndClear(boxId: Int) {
        readingListRepository.deleteLikeBoxAndClear(boxId)
    }

    fun deleteLikeBoxAndMoveToDefault(boxId: Int) {
        readingListRepository.deleteLikeBoxAndMoveToDefault(boxId)
    }

    fun renameLikeBox(boxId: Int, name: String) {
        readingListRepository.renameLikeBox(boxId, name)
    }
}
