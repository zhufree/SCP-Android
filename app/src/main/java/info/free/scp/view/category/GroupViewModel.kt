package info.free.scp.view.category

import androidx.lifecycle.ViewModel
import info.free.scp.bean.ScpModel
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import info.free.scp.util.PreferenceUtil

class GroupViewModel : ViewModel() {
    private val readDao = AppInfoDatabase.getInstance().likeAndReadDao()
    private val scpDao = ScpDatabase.getInstance().scpDao()
    private val categoryCount = PreferenceUtil.getCategoryCount()
    private var hasReadList = readDao.getHasReadList()

    fun getDocList(saveType: Int, groupIndex: Int = -1): List<ScpModel> {
        var docList = scpDao.getAllScpListByType(saveType).toMutableList() // 获取总列表
        if (groupIndex >= 0) {
            // 处理index 和offset
            val start = if (groupIndex == 0) 0 else groupIndex * categoryCount
            val limit = categoryCount
            val end = start + limit
            // 截取序列中的一部分
            if (docList.isNotEmpty() && start < docList.size) {
                docList = docList.subList(start, if (end < docList.size) end else docList.size - 1)
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