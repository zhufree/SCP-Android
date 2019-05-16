package info.free.scp.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel
import info.free.scp.bean.ScpRecordModel
import info.free.scp.util.PreferenceUtil


/**
 * Created by zhufree on 2018/8/27.
 * 封装一些数据库方法
 */


const val DB_VERSION = 4

class ScpDataHelper : SQLiteOpenHelper(ScpApplication.context, INFO_DB_NAME, null, DB_VERSION) {
    private var randomCount = 0

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ScpTable.CREATE_LIKE_AND_READ_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_VIEW_LIST_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL(ScpTable.CREATE_LIKE_AND_READ_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_VIEW_LIST_TABLE_SQL)
        }
    }

    /**
     * 获取某一type的scp list
     */
    fun getScpByTypeAndRange(type: Int, start: Int, range: Int): MutableList<ScpModel> {
        val end = start + range
        val resultList = emptyList<ScpModel>().toMutableList()
        if (PreferenceUtil.getIfHideFinished()) {
            resultList.addAll(ScpDatabase.getInstance().scpDao().getUnreadScpListByType(type))
        } else {
            resultList.addAll(ScpDatabase.getInstance().scpDao().getAllScpListByType(type))
        }
        resultList.sortBy {
            it.index
        }
        if (resultList.size == 0 || start > resultList.size) {
            return resultList
        }
        return resultList.subList(start, if (end < resultList.size) end else resultList.size - 1)
    }


    fun getSinglePageByType(type: Int): MutableList<ScpModel> {
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
        var resultList = ScpDatabase.getInstance().scpDao().getAllScpListByType(SCPConstants.ScpType.SINGLE_PAGE)
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


    fun getRandomScp(typeRange: String = ""): ScpModel? {
        randomCount++
        if (randomCount > 20) {
            randomCount = 0
            return null
        }
        var scpModel: ScpModel?
        var link = ""
        scpModel = if (typeRange.isEmpty()) ScpDatabase.getInstance().scpDao().getRandomScp()
        else ScpDatabase.getInstance().scpDao().getRandomScpByType(typeRange)

        if (scpModel != null) {
            link = scpModel.link
            val detailHtml = ScpDatabase.getInstance().detailDao().getDetail(link)
            scpModel = if (detailHtml.contains("抱歉，该页面尚无内容") || detailHtml.isEmpty())
                getRandomScp(typeRange) else ScpDatabase.getInstance().scpDao().getByLink(link)
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
