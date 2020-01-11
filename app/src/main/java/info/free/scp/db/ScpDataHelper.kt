package info.free.scp.db

import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.bean.ScpItemModel
import info.free.scp.bean.ScpLikeModel
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.util.PreferenceUtil


/**
 * Created by zhufree on 2018/8/27.
 * 封装一些数据库方法
 */


class ScpDataHelper {
    private var randomCount = 0

    /**
     * 获取某一type的scp list
     */
    fun getScpByTypeAndRange(type: Int, start: Int, range: Int): MutableList<ScpItemModel> {
        val end = start + range
        var queryList = emptyList<ScpItemModel>().toMutableList()
        val hasReadList = emptyList<ScpLikeModel>().toMutableList()
        // 数据库检索
        queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getAllScpListByType(type)?: emptyList())
        // 截取序列中的一部分
        if (queryList.size > 0 && start < queryList.size) {
            queryList = queryList.subList(start, if (end < queryList.size) end else queryList.size - 1)
        }
        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            hasReadList.addAll(AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList())
            queryList.removeAll { hasReadList.map { it_ ->
                it_.link
            }.contains(it.link) }
        }

        return queryList
    }
    fun getScpByType(type: Int): MutableList<ScpModel> {
        val queryList = emptyList<ScpModel>().toMutableList()
        val hasReadList = emptyList<ScpLikeModel>().toMutableList()
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
            hasReadList.addAll(AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList())
            queryList.removeAll { hasReadList.map { it_ ->
                it_.link
            }.contains(it.link) }
        }

        return queryList
    }
    fun getTalesByTypeAndSubType(type: Int, subType: String): MutableList<ScpModel> {
        val queryList = emptyList<ScpModel>().toMutableList()
        val hasReadList = emptyList<ScpLikeModel>().toMutableList()
        // 数据库检索
        queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getTalesByTypeAndSubType(type, subType)?: emptyList())
        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            hasReadList.addAll(AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList())
            queryList.removeAll { hasReadList.map { it_ ->
                it_.link
            }.contains(it.link) }
        }

        return queryList
    }

    fun getInternationalByCountry(country: String): MutableList<ScpModel> {
        val queryList = emptyList<ScpModel>().toMutableList()
        val hasReadList = emptyList<ScpLikeModel>().toMutableList()
        // 数据库检索
        queryList.addAll(ScpDatabase.getInstance()?.scpDao()?.getInternationalByCountry("$country%")
                ?: emptyList())
        if (PreferenceUtil.getHideFinished()) {
            // 去掉已读部分
            hasReadList.addAll(AppInfoDatabase.getInstance().likeAndReadDao().getHasReadList())
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
        var resultList = ScpDatabase.getInstance()?.scpDao()?.getAllScpListByType(SCPConstants.ScpType.SINGLE_PAGE)?: emptyList()
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


    fun getRandomScp(typeRange: String = ""): ScpItemModel? {
        randomCount++
        if (randomCount > 20) {
            randomCount = 0
            return null
        }
        var scpModel: ScpItemModel?
        var link = ""
        val args = typeRange.split(",")
        scpModel = if (typeRange.isEmpty()) ScpDatabase.getInstance()?.scpDao()?.getRandomScp()
        else ScpDatabase.getInstance()?.scpDao()?.getRandomScpByType(args[0], args[1])

        scpModel?.let { scp->
            link = scp.link
            val detailHtml = ScpDatabase.getInstance()?.detailDao()?.getDetail(link)
            detailHtml?.let {
                scpModel = if (it.contains("抱歉，该页面尚无内容") || it.isEmpty())
                    getRandomScp(typeRange) else scpModel
            }?:run{
                scpModel = getRandomScp(typeRange)
            }
        }
        return scpModel

    }


    fun insertViewListItem(link: String, title: String, viewType: Int) {
        var scpRecord = AppInfoDatabase.getInstance().readRecordDao().getInfoByLink(link)
        if (scpRecord == null) {
            scpRecord = ScpRecordModel(link, title, viewType)
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
