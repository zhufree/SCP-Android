package info.free.scp.data.reading

import info.free.scp.SCPConstants.HISTORY_TYPE
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpLikeBox
import info.free.scp.bean.ScpRecordModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingListRepositoryTest {
    @Test
    fun returnsDocumentStatusFromLikeAndRecordState() {
        val source = FakeReadingListDataSource().apply {
            likeInfo["/scp-001"] = ScpLikeModel("/scp-001", "SCP-001", like = true, hasRead = true, boxId = 0)
            records["/scp-001"] = ScpRecordModel("/scp-001", "SCP-001", LATER_TYPE)
        }
        val repository = ReadingListRepository(source)

        val status = repository.getDocumentStatus("/scp-001")

        assertTrue(status.isLiked)
        assertTrue(status.hasRead)
        assertTrue(status.isInLater)
    }

    @Test
    fun toggleReadCreatesMissingRecordAsRead() {
        val source = FakeReadingListDataSource()
        val repository = ReadingListRepository(source)

        repository.toggleRead("/scp-002", "SCP-002")

        assertEquals(
            ScpLikeModel("/scp-002", "SCP-002", like = false, hasRead = true, boxId = 0),
            source.likeInfo["/scp-002"]
        )
    }

    @Test
    fun toggleReadFlipsExistingReadState() {
        val source = FakeReadingListDataSource().apply {
            likeInfo["/scp-003"] = ScpLikeModel("/scp-003", "SCP-003", like = true, hasRead = true, boxId = 9)
        }
        val repository = ReadingListRepository(source)

        repository.toggleRead("/scp-003", "SCP-003")

        assertEquals(
            ScpLikeModel("/scp-003", "SCP-003", like = true, hasRead = false, boxId = 9),
            source.likeInfo["/scp-003"]
        )
    }

    @Test
    fun toggleLaterAddsMissingRecordAndReturnsTrue() {
        val source = FakeReadingListDataSource()
        val repository = ReadingListRepository(source)

        val isInLater = repository.toggleLater("/scp-004", "SCP-004")

        assertTrue(isInLater)
        assertEquals(LATER_TYPE, source.records["/scp-004"]?.viewListType)
    }

    @Test
    fun addToLaterConvertsExistingRecordToLater() {
        val source = FakeReadingListDataSource().apply {
            records["/scp-004"] = ScpRecordModel("/scp-004", "SCP-004", HISTORY_TYPE)
        }
        val repository = ReadingListRepository(source)

        repository.addToLater("/scp-004", "SCP-004")

        assertEquals(LATER_TYPE, source.records["/scp-004"]?.viewListType)
    }

    @Test
    fun toggleLaterRemovesExistingLaterRecordAndReturnsFalse() {
        val source = FakeReadingListDataSource().apply {
            records["/scp-005"] = ScpRecordModel("/scp-005", "SCP-005", LATER_TYPE)
        }
        val repository = ReadingListRepository(source)

        val isInLater = repository.toggleLater("/scp-005", "SCP-005")

        assertFalse(isInLater)
        assertFalse(source.records.containsKey("/scp-005"))
    }

    @Test
    fun getRecordListDelegatesToSourceOrder() {
        val expected = listOf(ScpRecordModel("/scp-006", "SCP-006", LATER_TYPE))
        val source = FakeReadingListDataSource().apply {
            recordLists[LATER_TYPE to ASC] = expected
        }
        val repository = ReadingListRepository(source)

        assertEquals(expected, repository.getRecordList(LATER_TYPE, ASC))
    }

    @Test
    fun getLikeBoxListCreatesDefaultBoxWhenEmpty() {
        val source = FakeReadingListDataSource()
        val repository = ReadingListRepository(source)

        val boxes = repository.getLikeBoxList()

        assertEquals(listOf(ScpLikeBox(0, "默认收藏夹")), boxes)
        assertEquals(listOf(ScpLikeBox(0, "默认收藏夹")), source.savedLikeBoxes)
    }

    @Test
    fun deleteLikeBoxAndClearUnlikesDocumentsBeforeDeletingBox() {
        val likes = listOf(
            ScpLikeModel("/scp-007", "SCP-007", like = true, hasRead = false, boxId = 2),
            ScpLikeModel("/scp-008", "SCP-008", like = true, hasRead = true, boxId = 2)
        )
        val source = FakeReadingListDataSource().apply {
            likesByBoxId[2] = likes
        }
        val repository = ReadingListRepository(source)

        repository.deleteLikeBoxAndClear(2)

        assertEquals(listOf(0, 0), source.savedLikeInfoBatches.single().map { it.boxId })
        assertTrue(source.savedLikeInfoBatches.single().none { it.like })
        assertEquals(listOf(2), source.deletedLikeBoxIds)
    }

    @Test
    fun deleteLikeBoxAndMovePreservesLikesInDefaultBox() {
        val likes = listOf(
            ScpLikeModel("/scp-009", "SCP-009", like = true, hasRead = false, boxId = 3)
        )
        val source = FakeReadingListDataSource().apply {
            likesByBoxId[3] = likes
        }
        val repository = ReadingListRepository(source)

        repository.deleteLikeBoxAndMoveToDefault(3)

        assertEquals(1, source.savedLikeInfoBatches.single().single().boxId)
        assertTrue(source.savedLikeInfoBatches.single().single().like)
        assertEquals(listOf(3), source.deletedLikeBoxIds)
    }

    @Test
    fun renameLikeBoxTrimsNameBeforeSaving() {
        val source = FakeReadingListDataSource().apply {
            savedLikeBoxes.add(ScpLikeBox(4, "Old"))
        }
        val repository = ReadingListRepository(source)

        repository.renameLikeBox(4, "  New name  ")

        assertEquals("New name", source.savedLikeBoxes.single().name)
    }

    @Test
    fun clearHistoryDelegatesToReadingDataSource() {
        val source = FakeReadingListDataSource()
        val repository = ReadingListRepository(source)

        repository.clearHistory()

        assertTrue(source.historyCleared)
    }

    @Test
    fun recordOpenedMovesLaterEntryToHistory() {
        val source = FakeReadingListDataSource().apply {
            records["/scp-010"] = ScpRecordModel("/scp-010", "SCP-010", LATER_TYPE)
        }
        val repository = ReadingListRepository(source)

        repository.recordOpened("/scp-010", "SCP-010")

        assertEquals(HISTORY_TYPE, source.records["/scp-010"]?.viewListType)
    }

    @Test
    fun setLikedPreservesReadStateAndMovesDocumentToSelectedBox() {
        val source = FakeReadingListDataSource().apply {
            likeInfo["/scp-011"] = ScpLikeModel(
                "/scp-011",
                "SCP-011",
                like = false,
                hasRead = true,
                boxId = 0
            )
        }
        val repository = ReadingListRepository(source)

        repository.setLiked("/scp-011", "SCP-011", boxId = 3)

        assertEquals(
            ScpLikeModel("/scp-011", "SCP-011", like = true, hasRead = true, boxId = 3),
            source.likeInfo["/scp-011"]
        )
    }

    @Test
    fun setUnlikedPreservesReadStateAndBox() {
        val source = FakeReadingListDataSource().apply {
            likeInfo["/scp-012"] = ScpLikeModel(
                "/scp-012",
                "SCP-012",
                like = true,
                hasRead = true,
                boxId = 4
            )
        }
        val repository = ReadingListRepository(source)

        repository.setUnliked("/scp-012", "SCP-012")

        assertEquals(
            ScpLikeModel("/scp-012", "SCP-012", like = false, hasRead = true, boxId = 4),
            source.likeInfo["/scp-012"]
        )
    }

    @Test
    fun createLikeBoxSavesRequestedName() {
        val source = FakeReadingListDataSource()
        val repository = ReadingListRepository(source)

        repository.createLikeBox("Research")

        assertEquals(listOf(ScpLikeBox(0, "Research")), source.savedLikeBoxes)
    }
}

private class FakeReadingListDataSource : ReadingListDataSource {
    val likeInfo = mutableMapOf<String, ScpLikeModel>()
    val records = mutableMapOf<String, ScpRecordModel>()
    val recordLists = mutableMapOf<Pair<Int, Int>, List<ScpRecordModel>>()
    val savedLikeBoxes = mutableListOf<ScpLikeBox>()
    val likesByBoxId = mutableMapOf<Int, List<ScpLikeModel>>()
    val savedLikeInfoBatches = mutableListOf<List<ScpLikeModel>>()
    val deletedLikeBoxIds = mutableListOf<Int>()
    var historyCleared = false

    override fun getLikeInfo(link: String): ScpLikeModel? = likeInfo[link]

    override fun saveLikeInfo(info: ScpLikeModel) {
        likeInfo[info.link] = info
    }

    override fun saveLikeInfos(info: List<ScpLikeModel>) {
        savedLikeInfoBatches.add(info)
        info.forEach { likeInfo[it.link] = it }
    }

    override fun deleteLikeInfo(info: ScpLikeModel) {
        likeInfo.remove(info.link)
    }

    override fun getRecord(link: String): ScpRecordModel? = records[link]

    override fun saveRecord(record: ScpRecordModel) {
        records[record.link] = record
    }

    override fun deleteRecord(link: String, viewType: Int) {
        if (records[link]?.viewListType == viewType) {
            records.remove(link)
        }
    }

    override fun deleteRecord(record: ScpRecordModel) {
        records.remove(record.link)
    }

    override fun getRecords(viewType: Int, order: Int): List<ScpRecordModel> {
        return recordLists[viewType to order].orEmpty()
    }

    override fun clearHistory() {
        historyCleared = true
    }

    override fun getLikeBoxes(): List<ScpLikeBox> = savedLikeBoxes

    override fun getLikeBox(boxId: Int): ScpLikeBox {
        return savedLikeBoxes.first { it.id == boxId }
    }

    override fun saveLikeBox(box: ScpLikeBox) {
        savedLikeBoxes.removeAll { it.id == box.id }
        savedLikeBoxes.add(box)
    }

    override fun deleteLikeBox(boxId: Int) {
        deletedLikeBoxIds.add(boxId)
        savedLikeBoxes.removeAll { it.id == boxId }
    }

    override fun getLikeListByBoxId(boxId: Int): List<ScpLikeModel> {
        return likesByBoxId[boxId].orEmpty()
    }
}
