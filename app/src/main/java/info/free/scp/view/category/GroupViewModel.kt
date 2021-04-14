package info.free.scp.view.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil
import kotlinx.coroutines.launch
import toast

class GroupViewModel : ViewModel() {
    private val readDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val scpDao = ScpDatabase.getInstance().scpDao()
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private var hasReadList = readDao.getHasReadList()
    private val abnormalPageList = arrayOf(
            "/log-of-extranormal-events",
            "/log-of-extranormal-events-cn",
            "/log-of-anomalous-items",
            "/log-of-anomalous-items-cn")
    private val introPageList = arrayOf(
            "/faq",
            "/guide-for-newbies",
            "/how-to-write-an-scp")
    private val infoPageList = arrayOf(
            "/secure-facilities-locations",
            "/secure-facilities-locations-cn",
            "/object-classes",
            "/security-clearance-levels",
            "/task-forces")

    fun getDocList(saveType: Int, groupIndex: Int = -1, extraType: String = ""): List<ScpModel> {
        var docList = when (saveType) {
            SCPConstants.ScpType.SAVE_SERIES, SCPConstants.ScpType.SAVE_SERIES_CN, SCPConstants.ScpType.SAVE_JOKE, SCPConstants.ScpType.SAVE_JOKE_CN, SCPConstants.ScpType.SAVE_EX, SCPConstants.ScpType.SAVE_EX_CN -> {
                scpDao.getAllScpListByType(saveType)
            }
            SCPConstants.ScpType.SAVE_TALES, SCPConstants.ScpType.SAVE_TALES_CN,
            SCPConstants.ScpType.SAVE_TALES_BY_TIME, SCPConstants.ScpType.SAVE_WANDER,
            SCPConstants.ScpType.SAVE_WANDER_CN -> {
                scpDao.getTalesByTypeAndSubType(saveType, extraType)
            }
            SCPConstants.ScpType.SAVE_CANON, SCPConstants.ScpType.SAVE_CANON_CN,
            SCPConstants.ScpType.SAVE_STORY_SERIES, SCPConstants.ScpType.SAVE_STORY_SERIES_CN,
            SCPConstants.ScpType.SAVE_CONTEST, SCPConstants.ScpType.SAVE_CONTEST_CN -> {
                scpDao.getAllScpListByType(saveType)
            }
            // 图书馆
            SCPConstants.Entry.LIBRARY_DOC -> {
                scpDao.getAllScpListByType(SCPConstants.ScpType.SAVE_LIBRARY_PAGE)
            }
            // 背景资料
            SCPConstants.Entry.INFORMATION_DOC -> {
                scpDao.getAllScpListByType(SCPConstants.ScpType.SAVE_INFO_PAGE)
            }
            SCPConstants.ScpType.SAVE_INTERNATIONAL -> {
                scpDao.getInternationalByCountry("$extraType%")
            }
            else -> {
                scpDao.getAllScpListByType(saveType)
            }
        }.toMutableList()
        if (groupIndex >= 0) {
            // 处理index 和offset
            val start = if (groupIndex == 0) 0 else groupIndex * categoryCount
            val limit = categoryCount
            val end = start + limit
            // 截取序列中的一部分
            if (docList.isNotEmpty() && start < docList.size) {
                docList = docList.subList(start, if (end < docList.size) end else docList.size - 1)
            } else {
                docList.clear()
                toast("数据不存在")
            }
        }
        // 去掉已读部分
        if (PreferenceUtil.getHideFinished()) {
            docList.removeAll {
                hasReadList.map { it_ ->
                    it_.link
                }.contains(it.link)
            }
        }
        return docList
    }


    private val randRepo = RandomRepository()

    fun getRandomList(): MutableLiveData<List<ScpItemModel>> {
        return randRepo.randomList
    }

    fun refreshRandomList(range: String) {
        randRepo.getRandomList(range)
    }
}