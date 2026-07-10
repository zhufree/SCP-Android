package info.free.scp.view.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.bean.ScpModel
import info.free.scp.data.tag.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagViewModel : ViewModel() {
    private val tagRepository = TagRepository()

    private val _tagScpList = MutableLiveData(listOf<ScpModel>())
    var tagScpList: LiveData<List<ScpModel>> = _tagScpList
        private set

    private val _tagMap = MutableLiveData(HashMap<String, Int>())
    var tagMap: LiveData<HashMap<String, Int>> = _tagMap
        private set

    fun getScpByTag(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _tagScpList.postValue(tagRepository.getScpByTag(tag))
        }
    }

    fun getTags() {
        viewModelScope.launch(Dispatchers.IO) {
            _tagMap.postValue(HashMap(tagRepository.getTags()))
        }
    }

    fun getTagText(link: String): String? {
        return tagRepository.getTagText(link)
    }
}
