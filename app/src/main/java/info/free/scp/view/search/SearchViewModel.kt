package info.free.scp.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.SCPConstants.SearchType.TITLE
import info.free.scp.bean.ScpModel
import info.free.scp.data.catalog.ScpCatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val catalogRepository = ScpCatalogRepository()
    var titleResult = emptyList<ScpModel>()
    var contentResult: MutableLiveData<List<ScpModel>> = MutableLiveData()

    fun searchScp(type: Int, keyword: String) {
        if (type == TITLE) {
            titleResult = catalogRepository.searchTitle(keyword)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                contentResult.postValue(catalogRepository.searchDetail(keyword))
            }
        }
    }
}
