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

    /**
     * 获取某一type的scp list
     */
    fun getScpByTypeAndRange(type: Int, start: Int, range: Int): MutableList<ScpItemModel> {
        val end = start + range
        var queryList = emptyList<ScpItemModel>().toMutableList()
        // 数据库检索
        queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getAllScpListByType(type)
                ?: emptyList())
        // 截取序列中的一部分
        if (queryList.size > 0 && start < queryList.size) {
            queryList = queryList.subList(start, if (end < queryList.size) end else queryList.size - 1)
        }
        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            queryList.removeAll {
                hasReadList.map { it_ ->
                    it_.link
                }.contains(it.link)
            }
        }

        return queryList
    }

    fun getScpByType(type: Int): MutableList<ScpModel> {
        val queryList = emptyList<ScpModel>().toMutableList()
        hasReadList = AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList()
        // 数据库检索
        if (type in (13..22)) {
            queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getAllCollectionByType(type)
                    ?: emptyList())
        } else {
            queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getAllScpListByType(type)
                    ?: emptyList())
        }

        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            queryList.removeAll {
                hasReadList.map { it_ ->
                    it_.link
                }.contains(it.link)
            }
        }

        return queryList
    }

    fun getTalesByTypeAndSubType(type: Int, subType: String): MutableList<ScpModel> {
        val queryList = emptyList<ScpModel>().toMutableList()
        hasReadList = AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList()
        // 数据库检索
        queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getTalesByTypeAndSubType(type, subType)
                ?: emptyList())
        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            queryList.removeAll {
                hasReadList.map { it_ ->
                    it_.link
                }.contains(it.link)
            }
        }

        return queryList
    }

    fun getInternationalByCountry(country: String): MutableList<ScpModel> {
        val queryList = emptyList<ScpModel>().toMutableList()
        hasReadList = AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList()
        // 数据库检索
        queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getInternationalByCountry("$country%")
                ?: emptyList())
        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            queryList.removeAll {
                hasReadList.map { it_ ->
                    it_.link
                }.contains(it.link)
            }
        }

        return queryList
    }


    fun getSinglePageByType(type: Int): MutableList<ScpItemModel> {
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
        var resultList = ScpDatabase.getInstance()?.scpDao()?.getAllScpListByType(SCPConstants.ScpType.SINGLE_PAGE)
                ?: emptyList()
        resultList = resultList.filter {
            when (type) {
                SCPConstants.ScpType.SAVE_INFO -> infoPageList.contains(it.link)
                SCPConstants.ScpType.SAVE_INTRO -> introPageList.contains(it.link)
                SCPConstants.ScpType.SAVE_ABNORMAL -> abnormalPageList.contains(it.link)
                else -> abnormalPageList.contains(it.link)
            }
        }.toMutableList()
        return resultList
    }


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
                    val detailHtml = ScpDatabase.getInstance()?.detailDao()?.getDetail(link)
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
            val addList = if (typeRange.isEmpty()) scpDao?.getRandomScp(leftCount)
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
