package info.free.scp.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel
import info.free.scp.bean.SimpleScp
import info.free.scp.util.Logger
import info.free.scp.util.PreferenceUtil


/**
 * Created by zhufree on 2018/8/27.
 * 数据库管理
 * 每次更新数据源时要删掉表重建
 * 所以已读数据和收藏需要单独建表存，以link为主键（存储时主键可能重复需要注意），存储时更新到总表中
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


    private fun syncLikeInfo() {
        with(readableDatabase) {
            val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.LIKE_AND_READ_TABLE_NAME, null)
            with(cursor) {
                this?.let {
                    while (it.moveToNext()) {
                        val link = getCursorString(cursor, ScpTable.LINK)
                        val like = getCursorInt(cursor, ScpTable.LIKE)
                        val hasRead = getCursorInt(cursor, ScpTable.HAS_READ)
                        val scpModels = getScpModelByLink(link)
                        scpModels.forEach { scp ->
                            scp?.like = like
                            scp?.hasRead = hasRead
//                            replaceScpModel(scp)
                        }
                    }
                }
            }
        }
    }


    fun insertLikeAndReadInfo(model: ScpModel) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            replaceLikeInfo(model)
//            replaceScpModel(model)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }



    private fun replaceLikeInfo(model: ScpModel) {
        val db = writableDatabase
        val cv = ContentValues()
        if (model.link.isNotEmpty()) {
            cv.put(ScpTable.LINK, model.link)
        }
        if (model.title.isNotEmpty()) {
            cv.put(ScpTable.TITLE, model.title)
        }
        cv.put(ScpTable.HAS_READ, model.hasRead)
        cv.put(ScpTable.LIKE, model.like)
        db.beginTransaction()
        try {
            if (getLikeInfoByLink(model.link)) {
                db.update(ScpTable.LIKE_AND_READ_TABLE_NAME, cv, ScpTable.LINK + "=?",
                        arrayOf(model.link))
            } else {
                db.insert(ScpTable.LIKE_AND_READ_TABLE_NAME, null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    /**
     * 通过链接获取scpModel，可能不止一个
     */
    fun getScpModelByLink(link: String?): MutableList<ScpModel?> {
        val resultList = emptyList<ScpModel?>().toMutableList()
        if (link == null) {
            return resultList
        }
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.LINK + "=?", arrayOf(link))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
            }
            return resultList

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    /**
     * 收藏列表只展示一个
     */
    fun getOneScpModelByLink(link: String): ScpModel? {
        val resultScps = getScpModelByLink(link)
        return if (resultScps.size > 0) resultScps[0] else null
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

    fun getLikeScpList(): MutableList<ScpModel?> {
        val resultList = emptyList<ScpModel?>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.LIKE_AND_READ_TABLE_NAME + " WHERE "
                        + ScpTable.LIKE + "=?;",
                        arrayOf("1"))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(getOneScpModelByLink(getCursorString(cursor, ScpTable.LINK)))
                        }
                    }
                }
            }
            return resultList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun getOrderedLikeList(): MutableList<ScpModel?> {
        val resultList = emptyList<ScpModel?>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.LIKE_AND_READ_TABLE_NAME + " as like_scp "
                        + " left join " + ScpTable.SCP_TABLE_NAME + " as scp on like_scp.link = scp.link WHERE like_scp."
                        + ScpTable.LIKE + "=? ORDER BY scp." + ScpTable.SCP_TYPE + ", scp." + ScpTable.INDEX + ";",
                        arrayOf("1"))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(getOneScpModelByLink(getCursorString(cursor, ScpTable.LINK)))
                        }
                    }
                }
            }
            return resultList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    private fun getLikeInfoByLink(link: String): Boolean {
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.LIKE_AND_READ_TABLE_NAME
                    + " WHERE " + ScpTable.LINK + "=?", arrayOf(link))
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return true
                }
                cursor.close()
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    fun getLikeCount(): Int {
        var count = 0
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT COUNT(*) FROM "
                    + ScpTable.LIKE_AND_READ_TABLE_NAME + " WHERE "
                    + ScpTable.LIKE + "=? ", arrayOf("1"))
            with(cursor) {
                this?.let {
                    if (it.moveToFirst()) {
                        count = it.getInt(0)
                    }
                }

            }
            return count
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }

    fun getReadCount(): Int {
        var count = 0
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT COUNT(*) FROM "
                    + ScpTable.LIKE_AND_READ_TABLE_NAME + " WHERE "
                    + ScpTable.HAS_READ + "=? ", arrayOf("1"))
            with(cursor) {
                this?.let {
                    if (it.moveToFirst()) {
                        count = it.getInt(0)
                    }
                }
            }
            return count
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }


    fun insertViewListItem(link: String, title: String, viewType: Int) {
        with(writableDatabase) {
            this.execSQL(ScpTable.INSERT_VIEW_LIST_SQL, arrayOf(link, title, viewType))
        }
    }

    fun deleteViewListItem(link: String, viewType: Int) {
        with(writableDatabase) {
            this.execSQL("DELETE FROM " + ScpTable.VIEW_LIST_TABLE_NAME + " WHERE " + ScpTable.LINK
                    + " = ?" + " AND " + ScpTable.VIEW_LIST_TYPE + " = " + viewType, arrayOf(link))
        }
    }

    fun getViewListByTypeAndOrder(type: Int, order: Int): MutableList<SimpleScp> {
        val resultList = emptyList<SimpleScp>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor = this.rawQuery("SELECT * FROM " + ScpTable.VIEW_LIST_TABLE_NAME + " WHERE " +
                        ScpTable.VIEW_LIST_TYPE + " = ? ORDER BY " + ScpTable.VIEW_TIME + if (order == 0) " ASC" else " DESC",
                        arrayOf(type.toString()))
                with(cursor) {
                    while (this.moveToNext()) {
                        val link = getCursorString(cursor, ScpTable.LINK)
                        val title = getCursorString(cursor, ScpTable.TITLE)
                        val time = getCursorString(cursor, ScpTable.VIEW_TIME)
                        resultList.add(SimpleScp(link, title, time))
                    }
                }
            }
            return resultList
        } catch (e: Exception) {
        }
        return resultList
    }

    /**
     * 删除目录表，在重新同步目录时调用
     */
    fun resetCategoryData() {
        with(writableDatabase) {
            PreferenceUtil.setInitCategoryFinish(false)
            this?.execSQL(ScpTable.dropScpTableSQL)
            this?.execSQL(ScpTable.CREATE_TABLE_SQL)
        }
    }

    companion object {
        private var scpDataHelper: ScpDataHelper? = ScpDataHelper()
        fun getInstance(): ScpDataHelper {
            return scpDataHelper ?: ScpDataHelper()
        }
    }

    /**
     * 工具方法封装一下
     */
    private fun getCursorString(cursor: Cursor?, columnIndex: String): String {
        return if (cursor?.getColumnIndex(columnIndex)?.compareTo(-1) == 1) {
            cursor.getString(cursor.getColumnIndex(columnIndex)) ?: ""
        } else {
            ""
        }
    }

    private fun getCursorInt(cursor: Cursor?, columnIndex: String): Int {
        return if (cursor?.getColumnIndex(columnIndex)?.compareTo(0) == 1) {
            cursor.getInt(cursor.getColumnIndex(columnIndex))
        } else {
            0
        }
    }


    /**
     * cursor转model取出
     */
    private fun extractScp(cursor: Cursor): ScpModel {
        randomCount = 0
        return ScpModel(-1,
                getCursorInt(cursor, ScpTable.INDEX),
                "", "",
                // 正文数据量太大，先不取出来，点击时再从数据库拿
                getCursorInt(cursor, ScpTable.SCP_TYPE),
                "",
                getCursorInt(cursor, ScpTable.DOWNLOAD_TYPE),
                getCursorString(cursor, ScpTable.SUB_TEXT),
                getCursorString(cursor, ScpTable.SNIPPET),
                getCursorString(cursor, ScpTable.DESC),
                getCursorString(cursor, ScpTable.AUTHOR),
                getCursorString(cursor, ScpTable.CREATOR),
                getCursorString(cursor, ScpTable.CREATED_TIME),
                getCursorString(cursor, ScpTable.TAGS),
                getCursorString(cursor, ScpTable.SUB_LINKS),
                getCursorInt(cursor, ScpTable.HAS_READ),
                getCursorInt(cursor, ScpTable.LIKE)
        )
    }
}
