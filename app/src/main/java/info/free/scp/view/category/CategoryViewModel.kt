package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.ScpModel
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private val catRepo = CategoryRepository()
    fun getCat(): MutableLiveData<List<ScpModel>>? {
        return catRepo.scpList
    }

    fun loadCat(feedType: Int) {
        viewModelScope.launch {
            catRepo.loadCatList(feedType)
        }
    }

    fun reverseCat() {
        catRepo.reverseCat()
    }
}
