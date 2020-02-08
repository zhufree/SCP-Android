package info.free.scp.view.portal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.PortalModel
import kotlinx.coroutines.launch

class PortalListViewModel : ViewModel() {
    private val repo = PortalRepository()
    fun getPortalList(): MutableLiveData<List<PortalModel>>? {
        return repo.portalList
    }

    fun loadPortalModels() {
        viewModelScope.launch {
            repo.loadPortalList()
        }
    }
}