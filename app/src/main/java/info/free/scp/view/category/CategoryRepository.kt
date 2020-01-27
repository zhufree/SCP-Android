package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import apiCall
import executeResponse
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.service.HttpManager

class CategoryRepository {
    var scpList = MutableLiveData<List<ScpModel>>()

    suspend fun loadCatList(scpType: Int, subScpType: String, limit: Int, start: Int, categoryType: Int) {
        val response = apiCall { HttpManager.instance.getCategory(scpType, subScpType, limit, start) }
        response?.let {
            executeResponse(response, {

            }, {
                if (categoryType in arrayOf(SCPConstants.Category.SCP_ABNORMAL,
                                SCPConstants.Category.ABOUT_INFO,
                                SCPConstants.Category.ABOUT_INTRO)) {
                    val filterResult = getSinglePageByType(response.results, categoryType)
                    if (categoryType == SCPConstants.Category.SCP_ABNORMAL) {
                        // 三句话外围
                        val shortStories = ScpModel(link = "/short-stories", title = "三句话外围")
                        val mutableResult = filterResult.toMutableList()
                        mutableResult.add(shortStories)
                        scpList.value = mutableResult
                    } else {
                        scpList.value = filterResult
                    }
                } else {
                    scpList.value = response.results

                }
            })
        }
    }

    fun getSinglePageByType(resultList: List<ScpModel>, type: Int): List<ScpModel> {
        val abnormalPageList = arrayOf(
                "/log-of-extranormal-events",
                "/log-of-extranormal-events-cn",
                "/log-of-anomalous-items",
                "/log-of-anomalous-items-cn")
        val introPageList = arrayOf(
                "/faq",
                "/guide-for-newbies",
                "/how-to-write-an-scp")
        val infoPageList = arrayOf(
                "/secure-facilities-locations",
                "/secure-facilities-locations-cn",
                "/object-classes",
                "/security-clearance-levels",
                "/task-forces")
        return resultList.filter {
            when (type) {
                SCPConstants.Category.ABOUT_INFO -> infoPageList.contains(it.link)
                SCPConstants.Category.ABOUT_INTRO -> introPageList.contains(it.link)
                SCPConstants.Category.SCP_ABNORMAL -> abnormalPageList.contains(it.link)
                else -> abnormalPageList.contains(it.link)
            }
        }
    }

    fun reverseCat() {
        val innerList = scpList.value?.toMutableList()
        innerList?.reverse()
        scpList.postValue(innerList)
    }
}