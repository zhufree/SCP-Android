package info.free.scp.db

import info.free.scp.SCPConstants
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.util.PreferenceUtil


/**
 * Created by zhufree on 2018/8/27.
 * 封装一些数据库方法
 */


class ScpDataHelper {
    private var randomCount = 0
    private var hasReadList = AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList()


    fun getRandomScpList(typeRange: String = ""): List<ScpItemModel>? {
        val randomList: MutableList<ScpItemModel> = emptyList<ScpItemModel>().toMutableList()
        randomCount++
        if (randomCount > 20) {
            randomCount = 0
            return randomList
        }
        hasReadList = AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList()
        val args = typeRange.split(",")
        // 先取10个 TODO 无collection
        val scpDao = ScpDatabase.getInstance()?.scpDao()
        val rawList = if (typeRange.isEmpty()) scpDao?.getRandomScp(10)
        else scpDao?.getRandomScpByType(args[0], args[1], 10)
        rawList?.let {
            randomList.addAll(it)
        }

        do {
            val removeIndexes = emptyList<Int>().toMutableList()
            randomList.forEachIndexed { index, scp ->
                val link = scp.link
                if (PreferenceUtil.getHideFinished() && link in (hasReadList.map { it.link })) {
                    // 已读过，重新随机
                    removeIndexes.add(index)
                } else {
                    val detailHtml = DetailDatabase.getInstance().detailDao().getDetail(link)
                    detailHtml?.let {
                        if (it.contains("null") || it.isEmpty()) {
                            removeIndexes.add(index)
                        }
                    } ?: run {
                        removeIndexes.add(index)
                    }
                }
            }
            for (i in removeIndexes) {
                randomList.removeAt(i)
            }
            val leftCount = 10 - randomList.size
            val addList = if (typeRange.isEmpty()) scpDao.getRandomScp(leftCount)
            else scpDao?.getRandomScpByType(args[0], args[1], leftCount)
            addList?.let {
                randomList.addAll(addList)
            }
        } while (randomList.size < 10)
        return randomList
    }


    fun insertViewListItem(link: String, title: String, viewType: Int) {
        var scpRecord = AppInfoDatabase.getInstance().readRecordDao().getInfoByLink(link)
        if (scpRecord == null) {
            scpRecord = ScpRecordModel(link, title, viewType)
        } else {
            scpRecord.viewListType = viewType
        }
        AppInfoDatabase.getInstance().readRecordDao().save(scpRecord)
    }


    fun getViewListByTypeAndOrder(type: Int, order: Int): MutableList<ScpRecordModel> {
        val resultList = emptyList<ScpRecordModel>().toMutableList()
        resultList.addAll(if (order == 0) AppInfoDatabase.getInstance().readRecordDao().getInfoByLinkAsc(type)
        else AppInfoDatabase.getInstance().readRecordDao().getInfoByLinkDesc(type))
        return resultList
    }


    companion object {
        private var scpDataHelper: ScpDataHelper? = ScpDataHelper()
        fun getInstance(): ScpDataHelper {
            return scpDataHelper ?: ScpDataHelper()
        }
    }
}
