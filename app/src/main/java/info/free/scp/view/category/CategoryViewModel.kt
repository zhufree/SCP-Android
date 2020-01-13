package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.db.ScpDataHelper
import info.free.scp.util.PreferenceUtil
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private val catRepo = CategoryRepository()
    private val categoryCount = PreferenceUtil.getCategoryCount()

    fun getCat(): MutableLiveData<List<ScpModel>>? {
        return catRepo.scpList
    }

    fun loadCat(scpType: Int, clickPosition: Int) {
        var start = 0
        var limit = 100
        when (scpType) {
            SCPConstants.Category.SERIES -> {
                // 0,499,999
                start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                limit = categoryCount
            }
            SCPConstants.Category.SERIES_CN -> {
                start = if (clickPosition == 0) 0 else clickPosition * categoryCount
                limit = categoryCount
            }
        }
        viewModelScope.launch {
            catRepo.loadCatList(scpType, limit, start)
        }
    }

    fun reverseCat() {
        catRepo.reverseCat()
    }
}
