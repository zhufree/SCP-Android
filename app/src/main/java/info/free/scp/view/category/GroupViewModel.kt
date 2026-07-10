package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.data.catalog.ScpCatalogRepository
import info.free.scp.data.random.RandomRepository

class GroupViewModel : ViewModel() {
    private val catalogRepository = ScpCatalogRepository()
    private val randomRepository = RandomRepository()
    private val randomList = MutableLiveData<List<ScpItemModel>>()

    fun getDocList(saveType: Int, groupIndex: Int = -1, extraType: String = ""): List<ScpModel> {
        return catalogRepository.getDocList(saveType, groupIndex, extraType)
    }

    fun getRandomList(): MutableLiveData<List<ScpItemModel>> {
        return randomList
    }

    fun refreshRandomList(range: String, add: Boolean = false) {
        val currentList = randomList.value
        if (add && currentList == null) return
        randomList.postValue(
            randomRepository.getRandomList(
                range = range,
                currentList = currentList.orEmpty(),
                append = add
            )
        )
    }
}
