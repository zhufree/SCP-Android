package info.free.scp.view.tag

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.DetailDatabase
import info.free.scp.db.ScpDatabase

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
        detailDao?.let {
            val scpLinks = detailDao.getLinksByTag("%$tag%")
            scpDao?.let {
                val queryCounts = scpLinks.size / 10 + 1
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
        }
    }

    fun getTags() {
        val allTagStr = detailDao?.getAllTags()?.filter { !it.isNullOrEmpty() }
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
    }
}