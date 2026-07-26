package info.free.scp.view.category

import androidx.lifecycle.ViewModel
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.OrderType.ASC
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.data.random.RandomRepository
import info.free.scp.data.reading.ReadingListRepository
import info.free.scp.data.settings.InteractionPointsStore
import info.free.scp.data.settings.PreferenceSettingsStore
import info.free.scp.ui.category.ScpDocumentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CategoryListUiState(
    val documents: List<ScpModel> = emptyList(),
    val orderType: Int = ASC,
    val scrollToTopSignal: Int = 0
)

data class RandomListUiState(
    val documents: List<ScpItemModel> = emptyList(),
    val initialized: Boolean = false
)

class GroupViewModel : ViewModel() {
    private val catalogRepository = ScpCatalogRepository()
    private val randomRepository = RandomRepository()
    private val readingListRepository = ReadingListRepository()
    private val interactionPoints: InteractionPointsStore = PreferenceSettingsStore()
    private val _categoryUiState = MutableStateFlow(CategoryListUiState())
    val categoryUiState: StateFlow<CategoryListUiState> = _categoryUiState.asStateFlow()
    private val _randomUiState = MutableStateFlow(RandomListUiState())
    val randomUiState: StateFlow<RandomListUiState> = _randomUiState.asStateFlow()
    private var currentQuery = CategoryQuery()

    fun loadDocuments(saveType: Int, groupIndex: Int = -1, extraType: String = "") {
        currentQuery = CategoryQuery(saveType, groupIndex, extraType)
        updateDocuments()
    }

    fun setDocumentOrder(orderType: Int) {
        _categoryUiState.update {
            it.copy(
                orderType = orderType,
                scrollToTopSignal = it.scrollToTopSignal + 1
            )
        }
        updateDocuments()
    }

    fun reverseDocumentOrder() {
        setDocumentOrder(-_categoryUiState.value.orderType)
    }

    fun getDocumentStatus(scp: ScpModel): ScpDocumentStatus {
        val status = readingListRepository.getDocumentStatus(scp.link)
        return ScpDocumentStatus(
            isLiked = status.isLiked,
            hasRead = status.hasRead,
            isInLater = status.isInLater
        )
    }

    fun toggleRead(scp: ScpModel) {
        readingListRepository.toggleRead(scp.link, scp.title)
    }

    fun toggleLater(scp: ScpModel): Boolean {
        return readingListRepository.toggleLater(scp.link, scp.title)
    }

    fun rewardDocumentOpen() {
        interactionPoints.increasePoints(2)
    }

    fun refreshRandomList(range: String, add: Boolean = false) {
        val currentState = _randomUiState.value
        if (add && !currentState.initialized) return
        _randomUiState.value = RandomListUiState(
            documents = randomRepository.getRandomList(
                range = range,
                currentList = currentState.documents,
                append = add
            ),
            initialized = true
        )
    }

    private fun updateDocuments() {
        val documents = catalogRepository.getDocList(
            saveType = currentQuery.saveType,
            groupIndex = currentQuery.repositoryGroupIndex,
            extraType = currentQuery.repositoryExtraType
        )
        _categoryUiState.update { state ->
            state.copy(
                documents = if (state.orderType == ASC) documents else documents.reversed()
            )
        }
    }
}

private data class CategoryQuery(
    val saveType: Int = 1,
    val groupIndex: Int = -1,
    val extraType: String = ""
) {
    val repositoryGroupIndex: Int
        get() = when (saveType) {
            SCPConstants.ScpType.SAVE_TALES,
            SCPConstants.ScpType.SAVE_TALES_CN,
            SCPConstants.ScpType.SAVE_TALES_BY_TIME,
            SCPConstants.ScpType.SAVE_WANDER,
            SCPConstants.ScpType.SAVE_WANDER_CN,
            SCPConstants.ScpType.SAVE_CANON,
            SCPConstants.ScpType.SAVE_CANON_CN,
            SCPConstants.ScpType.SAVE_STORY_SERIES,
            SCPConstants.ScpType.SAVE_STORY_SERIES_CN,
            SCPConstants.ScpType.SAVE_CONTEST,
            SCPConstants.ScpType.SAVE_CONTEST_CN,
            SCPConstants.ScpType.SAVE_INTERNATIONAL,
            SCPConstants.Entry.LIBRARY_DOC -> -1
            else -> groupIndex
        }

    val repositoryExtraType: String
        get() = when (saveType) {
            SCPConstants.ScpType.SAVE_TALES,
            SCPConstants.ScpType.SAVE_TALES_CN,
            SCPConstants.ScpType.SAVE_TALES_BY_TIME,
            SCPConstants.ScpType.SAVE_WANDER,
            SCPConstants.ScpType.SAVE_WANDER_CN,
            SCPConstants.ScpType.SAVE_INTERNATIONAL -> extraType
            else -> ""
        }
}
