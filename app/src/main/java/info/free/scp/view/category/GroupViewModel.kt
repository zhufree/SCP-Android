package info.free.scp.view.category

import androidx.lifecycle.ViewModel
import info.free.scp.SCPConstants
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil
import toast

class GroupViewModel : ViewModel() {
    private val readDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val scpDao = ScpDatabase.getInstance().scpDao()
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private var hasReadList = readDao.getHasReadList()

    fun getDocList(saveType: Int, groupIndex: Int = -1, extraType: String = ""): List<ScpModel> {
        var docList = when (saveType) {
            SCPConstants.ScpType.SAVE_SERIES, SCPConstants.ScpType.SAVE_SERIES_CN, SCPConstants.ScpType.SAVE_JOKE, SCPConstants.ScpType.SAVE_JOKE_CN, SCPConstants.ScpType.SAVE_EX, SCPConstants.ScpType.SAVE_EX_CN -> {
                scpDao.getAllScpListByType(saveType)
            }
            SCPConstants.ScpType.SAVE_TALES, SCPConstants.ScpType.SAVE_TALES_CN, SCPConstants.ScpType.SAVE_TALES_BY_TIME -> {
                scpDao.getTalesByTypeAndSubType(saveType, extraType)
            }
            SCPConstants.ScpType.SAVE_SETTINGS, SCPConstants.ScpType.SAVE_SETTINGS_CN,
            SCPConstants.ScpType.SAVE_STORY_SERIES, SCPConstants.ScpType.SAVE_STORY_SERIES_CN,
            SCPConstants.ScpType.SAVE_CONTEST, SCPConstants.ScpType.SAVE_CONTEST_CN -> {
                scpDao.getAllCollectionByType(saveType)
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
//        when (categoryType) {
//            SCP_DOC -> {
//                scpDao.getAllScpListByType(categoryType)
//            }
//        }
    }
}