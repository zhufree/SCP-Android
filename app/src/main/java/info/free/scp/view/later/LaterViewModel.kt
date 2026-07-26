package info.free.scp.view.later

import androidx.lifecycle.ViewModel
import info.free.scp.SCPConstants.LATER_TYPE
import info.free.scp.SCPConstants.OrderType.DESC
import info.free.scp.bean.*
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.data.reading.ReadListImportParser
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.data.settings.ReadingPreferencesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LaterUiState(
    val records: List<ScpRecordModel> = emptyList(),
    val recordType: Int = LATER_TYPE,
    val recordOrder: Int = DESC,
    val recordScrollToTopSignal: Int = 0,
    val likeBoxes: List<ScpLikeBox> = emptyList(),
    val likes: List<ScpLikeModel> = emptyList(),
    val availableLikeBoxes: List<ScpLikeBox> = emptyList(),
    val selectedLikeBoxId: Int = 0,
    val sortLikesByTitle: Boolean = false
)

class LaterViewModel : ViewModel() {
    private val readingListRepository = ReadingListRepository()
    private val catalogRepository = ScpCatalogRepository()
    private val readingPreferences: ReadingPreferencesStore = PreferenceSettingsStore()
    private val _uiState = MutableStateFlow(LaterUiState())
    val uiState: StateFlow<LaterUiState> = _uiState.asStateFlow()

    fun loadRecords(type: Int, order: Int = _uiState.value.recordOrder, scrollToTop: Boolean = false) {
        _uiState.update {
            it.copy(
                records = readingListRepository.getRecordList(type, order),
                recordType = type,
                recordOrder = order,
                recordScrollToTopSignal = if (scrollToTop) {
                    it.recordScrollToTopSignal + 1
                } else {
                    it.recordScrollToTopSignal
                }
            )
        }
    }

    fun deleteRecord(record: ScpRecordModel) {
        readingListRepository.deleteRecord(record)
        loadRecords(_uiState.value.recordType)
    }

    fun clearHistory() {
        readingListRepository.clearHistory()
        loadRecords(_uiState.value.recordType)
    }

    fun loadLikeBoxes() {
        _uiState.update { it.copy(likeBoxes = readingListRepository.getLikeBoxList()) }
    }

    fun loadLikes(boxId: Int, sortByTitle: Boolean = _uiState.value.sortLikesByTitle) {
        val orderedLikes = buildList {
            if (boxId == 1) {
                addAll(readingListRepository.getLikeListByBoxId(0))
            }
            addAll(readingListRepository.getLikeListByBoxId(boxId))
        }
        _uiState.update {
            it.copy(
                likes = if (sortByTitle) orderedLikes.sortedBy { like -> like.title } else orderedLikes,
                availableLikeBoxes = readingListRepository.getExistingLikeBoxes(),
                selectedLikeBoxId = boxId,
                sortLikesByTitle = sortByTitle
            )
        }
    }

    fun reverseLikeOrder() {
        loadLikes(
            boxId = _uiState.value.selectedLikeBoxId,
            sortByTitle = !_uiState.value.sortLikesByTitle
        )
    }

    fun deleteLike(like: ScpLikeModel) {
        readingListRepository.deleteLike(like)
        loadLikes(_uiState.value.selectedLikeBoxId)
    }

    fun moveLike(like: ScpLikeModel, boxId: Int) {
        readingListRepository.moveLike(like, boxId)
        loadLikes(_uiState.value.selectedLikeBoxId)
    }

    fun deleteLikeBoxAndClear(boxId: Int) {
        readingListRepository.deleteLikeBoxAndClear(boxId)
        loadLikeBoxes()
    }

    fun deleteLikeBoxAndMoveToDefault(boxId: Int) {
        readingListRepository.deleteLikeBoxAndMoveToDefault(boxId)
        loadLikeBoxes()
    }

    fun renameLikeBox(boxId: Int, name: String) {
        readingListRepository.renameLikeBox(boxId, name)
        loadLikeBoxes()
    }

    fun shouldShowReadSuggestion(): Boolean = readingPreferences.shouldShowReadSuggestion

    fun markReadSuggestionShown() {
        readingPreferences.markReadSuggestionShown()
    }

    fun importReadList(input: String): Boolean {
        val requests = ReadListImportParser.parse(input)
        if (requests.isEmpty()) {
            return false
        }
        requests.forEach { request ->
            catalogRepository.getDocumentByNumber(request.scpType, request.numberPattern)
                ?.let { document ->
                    readingListRepository.addToLater(document.link, document.title)
                }
        }
        loadRecords(LATER_TYPE)
        return true
    }
}
