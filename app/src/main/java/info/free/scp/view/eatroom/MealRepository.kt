package info.free.scp.view.eatroom

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.bean.MealModel
import info.free.scp.service.FreeService
import toast


class MealRepository {
    var mealList: MutableLiveData<List<MealModel>> = MutableLiveData()

    suspend fun loadMealList(): MutableLiveData<List<MealModel>> {

        val response = apiCall { FreeService.getAllMeal() }
        response?.let {
            executeResponse(response, {
                toast("加载失败")
            }, {
                mealList.postValue(response.results)
            })
        }
        return mealList
    }
}