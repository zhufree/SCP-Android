package info.free.scp.view.eatroom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.MealModel
import kotlinx.coroutines.launch

class MealListViewModel : ViewModel() {
    private val repo = MealRepository()
    fun getMealList(): MutableLiveData<List<MealModel>>? {
        return repo.mealList
    }

    fun loadMealModels() {
        viewModelScope.launch {
            repo.loadMealList()
        }
    }
}