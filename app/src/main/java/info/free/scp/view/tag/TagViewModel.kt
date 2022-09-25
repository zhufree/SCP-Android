package info.free.scp.view.tag

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apiCall
import info.free.scp.bean.ScpModel
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.service.HttpManager
import kotlinx.coroutines.launch

class TagViewModel : ViewModel() {
    private var _tagScpList = MutableLiveData(listOf<ScpModel>())
    var tagScpList: LiveData<List<ScpModel>> = _tagScpList
        private set

    private var _tagMap = MutableLiveData(HashMap<String, Int>())
    var tagMap: LiveData<HashMap<String, Int>> = _tagMap
        private set

    private val scpDao = ScpDatabase.getInstance()?.scpDao()
    private val detailDao = DetailDatabase.getInstance()?.detailDao()

    fun getScpByTag(tag: String) {
        viewModelScope.launch {
            detailDao?.let {
                val scpLinks = detailDao.getLinksByTag("%$tag%")
                scpDao?.let {
                    val queryCounts =
                        if (scpLinks.size % 10 == 0) (scpLinks.size / 10) else (scpLinks.size / 10 + 1) // 10个一组，最后一组不足10个
                    val resultScpList = emptyList<ScpModel>().toMutableList()
                    for (i in 0 until queryCounts) {
                        Log.i("tag", i.toString())
                        if (i < queryCounts - 1) {
                            resultScpList.addAll(
                                scpDao.getScpListByLinks(
                                    scpLinks.subList(
                                        i * 10,
                                        (i + 1) * 10
                                    )
                                )
                            )
                        } else {
                            // i == queryCounts-1
                            resultScpList.addAll(
                                scpDao.getScpListByLinks(
                                    scpLinks.subList(
                                        i * 10,
                                        scpLinks.size - 1
                                    )
                                )
                            )
                        }
                    }
                    _tagScpList.value = resultScpList
                }
            } ?: run {
                val response = apiCall { HttpManager.instance.getScpByTag(tag) }
                response?.let {
                    if (response.results.isNotEmpty()) {
                        _tagScpList.value = response.results
                    }
                }
            }
        }
    }

    fun getTags() {
        detailDao?.let {
            val allTagStr = detailDao.getAllTags()?.filter { !it.isNullOrEmpty() }
            val tagCountMap = HashMap<String, Int>()
            allTagStr?.forEach {
                val tagList = it?.split(",")
                tagList?.forEach { tag ->
                    if (tag in tagCountMap.keys) {
                        val count = tagCountMap[tag]?.plus(1) ?: 0
                        tagCountMap[tag] = count
                    } else {
                        tagCountMap[tag] = 0
                    }
                }
            }
            _tagMap.value = tagCountMap
        } ?: run {
            viewModelScope.launch {
                val response = apiCall { HttpManager.instance.getAllTags() }
                response?.let {
                    val tagCountMap = HashMap<String, Int>()
                    if (response.results.isNotEmpty()) {
                        response.results.map { tagCountMap[it.name] = it.count }
                        _tagMap.value = tagCountMap
                    }
                }
            }
        }
    }
}