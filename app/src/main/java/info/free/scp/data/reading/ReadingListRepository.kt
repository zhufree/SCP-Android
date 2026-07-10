package info.free.scp.data.reading

import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.db.AppInfoDatabase

data class ReadingDocumentStatus(
    val isLiked: Boolean,
    val hasRead: Boolean,
    val isInLater: Boolean
)

interface ReadingListDataSource {
    fun getLikeInfo(link: String): ScpLikeModel?
    fun saveLikeInfo(info: ScpLikeModel)
    fun saveLikeInfos(info: List<ScpLikeModel>)
    fun deleteLikeInfo(info: ScpLikeModel)
    fun getRecord(link: String): ScpRecordModel?
    fun saveRecord(record: ScpRecordModel)
    fun deleteRecord(link: String, viewType: Int)
    fun deleteRecord(record: ScpRecordModel)
    fun getRecords(viewType: Int, order: Int): List<ScpRecordModel>
    fun clearHistory()
    fun getLikeBoxes(): List<ScpLikeBox>
    fun getLikeBox(boxId: Int): ScpLikeBox
    fun saveLikeBox(box: ScpLikeBox)
    fun deleteLikeBox(boxId: Int)
    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel>
}

class RoomReadingListDataSource : ReadingListDataSource {
    private val likeDao by lazy { AppInfoDatabase.getInstance().likeAndReadDao() }
    private val recordDao by lazy { AppInfoDatabase.getInstance().readRecordDao() }

    override fun getLikeInfo(link: String): ScpLikeModel? = likeDao.getInfoByLink(link)

    override fun saveLikeInfo(info: ScpLikeModel) {
        likeDao.save(info)
    }

    override fun saveLikeInfos(info: List<ScpLikeModel>) {
        likeDao.saveAll(info)
    }

    override fun deleteLikeInfo(info: ScpLikeModel) {
        likeDao.deleteLike(info)
    }

    override fun getRecord(link: String): ScpRecordModel? = recordDao.getInfoByLink(link)

    override fun saveRecord(record: ScpRecordModel) {
        recordDao.save(record)
    }

    override fun deleteRecord(link: String, viewType: Int) {
        recordDao.delete(link, viewType)
    }

    override fun deleteRecord(record: ScpRecordModel) {
        recordDao.delete(record)
    }

    override fun getRecords(viewType: Int, order: Int): List<ScpRecordModel> {
        return if (order == ASC) {
            recordDao.getInfoByLinkAsc(viewType)
        } else {
            recordDao.getInfoByLinkDesc(viewType)
        }
    }

    override fun clearHistory() {
        recordDao.clearHistory()
    }

    override fun getLikeBoxes(): List<ScpLikeBox> = likeDao.getLikeBox()

    override fun getLikeBox(boxId: Int): ScpLikeBox = likeDao.getLikeBoxById(boxId)

    override fun saveLikeBox(box: ScpLikeBox) {
        likeDao.saveLikeBox(box)
    }

    override fun deleteLikeBox(boxId: Int) {
        likeDao.deleteLikeBoxById(boxId)
    }

    override fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel> {
        return likeDao.getLikeListByBoxId(boxId)
    }
}

class ReadingListRepository(
    private val dataSource: ReadingListDataSource = RoomReadingListDataSource()
) {
    fun getDocumentStatus(link: String): ReadingDocumentStatus {
        val likeInfo = dataSource.getLikeInfo(link)
        val record = dataSource.getRecord(link)
        return ReadingDocumentStatus(
            isLiked = likeInfo?.like == true,
            hasRead = likeInfo?.hasRead == true,
            isInLater = record?.viewListType == LATER_TYPE
        )
    }

    fun getLikeInfo(link: String, title: String): ScpLikeModel {
        return dataSource.getLikeInfo(link)
            ?: ScpLikeModel(link, title, like = false, hasRead = false, boxId = 0)
    }

    fun toggleRead(link: String, title: String): Boolean {
        val current = getLikeInfo(link, title)
        val updated = current.copy(hasRead = !current.hasRead)
        dataSource.saveLikeInfo(updated)
        return updated.hasRead
    }

    fun setLiked(link: String, title: String, boxId: Int) {
        val current = getLikeInfo(link, title)
        dataSource.saveLikeInfo(current.copy(like = true, boxId = boxId))
    }

    fun setUnliked(link: String, title: String) {
        val current = getLikeInfo(link, title)
        dataSource.saveLikeInfo(current.copy(like = false))
    }

    fun toggleLater(link: String, title: String): Boolean {
        val current = dataSource.getRecord(link)
        if (current?.viewListType == LATER_TYPE) {
            dataSource.deleteRecord(link, LATER_TYPE)
            return false
        }

        addToLater(link, title)
        return true
    }

    fun addToLater(link: String, title: String) {
        val current = dataSource.getRecord(link)
        val target = current ?: ScpRecordModel(link, title, LATER_TYPE)
        target.viewListType = LATER_TYPE
        dataSource.saveRecord(target)
    }

    fun recordOpened(link: String, title: String) {
        val current = dataSource.getRecord(link)
        val historyRecord = current ?: ScpRecordModel(link, title, HISTORY_TYPE)
        historyRecord.viewListType = HISTORY_TYPE
        dataSource.saveRecord(historyRecord)
        dataSource.deleteRecord(link, LATER_TYPE)
    }

    fun getRecordList(type: Int, order: Int): List<ScpRecordModel> {
        return dataSource.getRecords(type, order)
    }

    fun deleteRecord(record: ScpRecordModel) {
        dataSource.deleteRecord(record)
    }

    fun clearHistory() {
        dataSource.clearHistory()
    }

    fun getLikeBoxList(): List<ScpLikeBox> {
        val boxList = dataSource.getLikeBoxes().toMutableList()
        if (boxList.isEmpty()) {
            val defaultBox = ScpLikeBox(0, "默认收藏夹")
            boxList.add(defaultBox)
            dataSource.saveLikeBox(defaultBox)
        }
        return boxList
    }

    fun createLikeBox(name: String) {
        dataSource.saveLikeBox(ScpLikeBox(0, name))
    }

    fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel> {
        return dataSource.getLikeListByBoxId(boxId)
    }

    fun getExistingLikeBoxes(): List<ScpLikeBox> {
        return dataSource.getLikeBoxes()
    }

    fun deleteLike(info: ScpLikeModel) {
        dataSource.deleteLikeInfo(info)
    }

    fun moveLike(info: ScpLikeModel, boxId: Int) {
        info.boxId = boxId
        dataSource.saveLikeInfo(info)
    }

    fun deleteLikeBoxAndClear(boxId: Int) {
        if (boxId == DEFAULT_BOX_ID) return
        val likes = dataSource.getLikeListByBoxId(boxId)
        likes.forEach { like ->
            like.boxId = UNFILED_BOX_ID
            like.like = false
        }
        dataSource.saveLikeInfos(likes)
        dataSource.deleteLikeBox(boxId)
    }

    fun deleteLikeBoxAndMoveToDefault(boxId: Int) {
        if (boxId == DEFAULT_BOX_ID) return
        val likes = dataSource.getLikeListByBoxId(boxId)
        likes.forEach { like -> like.boxId = DEFAULT_BOX_ID }
        dataSource.saveLikeInfos(likes)
        dataSource.deleteLikeBox(boxId)
    }

    fun renameLikeBox(boxId: Int, name: String) {
        if (boxId == DEFAULT_BOX_ID) return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        val box = dataSource.getLikeBox(boxId)
        box.name = trimmedName
        dataSource.saveLikeBox(box)
    }

    private companion object {
        const val UNFILED_BOX_ID = 0
        const val DEFAULT_BOX_ID = 1
    }
}
