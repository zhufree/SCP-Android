package info.free.scp.view.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.ScpModel
import info.free.scp.data.catalog.ScpCatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val keyword: String = "",
    val selectedTab: Int = 0,
    val titleResults: List<ScpModel> = emptyList(),
    val contentResults: List<ScpModel> = emptyList(),
    val contentLoading: Boolean = false,
    val contentSearched: Boolean = false,
    val needsOfflineData: Boolean = false,
    val titleSearched: Boolean = false
)

class SearchViewModel : ViewModel() {
    private val catalogRepository = ScpCatalogRepository()
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun startSearch(keyword: String) {
        if (_uiState.value.keyword == keyword && _uiState.value.titleSearched) return
        _uiState.value = SearchUiState(keyword = keyword)
        viewModelScope.launch(Dispatchers.IO) {
            val results = catalogRepository.searchTitle("%$keyword%")
            _uiState.update {
                it.copy(titleResults = results, titleSearched = true)
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == 1 && !_uiState.value.contentSearched) {
            searchContent()
        }
    }

    private fun searchContent() {
        if (!catalogRepository.isDetailSearchAvailable()) {
            _uiState.update { it.copy(needsOfflineData = true) }
            return
        }
        _uiState.update {
            it.copy(
                contentLoading = true,
                contentSearched = true,
                needsOfflineData = false
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val results = catalogRepository.searchDetail("%${_uiState.value.keyword}%")
            _uiState.update {
                it.copy(contentResults = results, contentLoading = false)
            }
        }
    }
}
