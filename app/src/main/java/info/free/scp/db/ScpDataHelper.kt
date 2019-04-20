package info.free.scp.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.DB_NAME
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel
import info.free.scp.bean.SimpleScp
import info.free.scp.util.Logger
import info.free.scp.util.PreferenceUtil
import java.util.*


/**
 * Created by zhufree on 2018/8/27.
 * 数据库管理
 * 每次更新数据源时要删掉表重建
 * 所以已读数据和收藏需要单独建表存，以link为主键（存储时主键可能重复需要注意），存储时更新到总表中
 */


const val DB_VERSION = 4

class ScpDataHelper : SQLiteOpenHelper(ScpApplication.context, DB_NAME, null, DB_VERSION) {
    private var randomCount = 0

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ScpTable.CREATE_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_LIKE_AND_READ_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_VIEW_LIST_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL(ScpTable.dropScpTableSQL)
            db?.execSQL(ScpTable.dropDetailTableSQL)
            db?.execSQL(ScpTable.CREATE_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_LIKE_AND_READ_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_VIEW_LIST_TABLE_SQL)
        }
    }

    fun resetDb() {
        Logger.i("resetDb")
        PreferenceUtil.setInitCategoryFinish(false)
        for (i in 0..SCPConstants.Download.DOWNLOAD_TOTAL) {
            PreferenceUtil.resetSingleDbLoadCount(i)
            PreferenceUtil.setDetailLastLoadTime(i, "")
        }
        with(writableDatabase) {
            this?.execSQL(ScpTable.dropScpTableSQL)
            this?.execSQL(ScpTable.dropDetailTableSQL)
            this?.execSQL(ScpTable.CREATE_TABLE_SQL)
            this?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
        }
    }

    fun insertCategoryData(models: List<ScpModel>) {
        if (models.isEmpty()) {
            return
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.createStatement(models)
            syncLikeInfo()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
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
                            replaceScpModel(scp)
                        }
                    }
                }
            }
        }
    }

    fun insertDetailData(models: List<ScpModel>) {
        if (models.isEmpty()) {
            return
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.createDetailStatement(models)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertLikeAndReadInfo(model: ScpModel) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            replaceLikeInfo(model)
            replaceScpModel(model)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun SQLiteDatabase.createStatement(models: List<ScpModel>) {
        try {
            val stmt = compileStatement(ScpTable.INSERT_SCP_SQL)
            for (model in models) {
                stmt.bindString(1, model.sId)
                stmt.bindLong(2, model.index.toLong())
                stmt.bindString(3, model.link)
                stmt.bindString(4, model.title)
                stmt.bindLong(5, model.scpType.toLong())
                stmt.bindLong(6, model.downloadType.toLong())
                stmt.bindString(7, model.subtext)
                stmt.bindString(8, model.snippet)
                stmt.bindString(9, model.desc)
                stmt.bindString(10, model.author)
                stmt.bindString(11, model.creator)
                stmt.bindString(12, model.createdTime)
                stmt.bindString(13, model.tags)
                stmt.bindString(14, model.subLinks)
                stmt.bindLong(15, model.like.toLong())
                stmt.bindLong(16, model.hasRead.toLong())
                Log.i("loading", "sid = ${model.sId}")
                stmt.execute()
                stmt.clearBindings()
            }
        } catch (e: Exception) {
        }
    }

    private fun SQLiteDatabase.createDetailStatement(models: List<ScpModel>) {
        try {
            val stmt = compileStatement(ScpTable.INSERT_DETAIL_SQL)
            for (model in models) {
                stmt.bindString(1, model.link)
                stmt.bindLong(3, model.downloadType.toLong())
                stmt.execute()
                stmt.clearBindings()
            }
        } catch (e: Exception) {
            Log.i("detail", e.message)
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
     * 更新读过和like信息
     * 返回更新后的model
     */
    private fun replaceScpModel(model: ScpModel?) {
        if (model == null || model.link.isEmpty()) {
            return
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = packScp(model)
            if (getScpModelById(model.sId) == null) {
                // 如果之前没有，直接存储
                db.insert(ScpTable.SCP_TABLE_NAME, null, cv)
            } else {
                // 如果之前有了，更新字段，阅读的信息只有新数据> 0时才会更新，所以不会覆盖
                db.update(ScpTable.SCP_TABLE_NAME, cv, ScpTable.ID + "=?",
                        arrayOf(model.sId))
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

    fun getScpModelById(id: String?): ScpModel? {
        if (id == null) {
            return null
        }
        try {
            var scpModel: ScpModel? = null
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.ID + "=?", arrayOf(id))
                with(cursor) {
                    this?.let {
                        if (it.moveToFirst()) {
                            scpModel = extractScp(it)
                        }
                    }
                }
            }
            return scpModel

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getScpByTypeAndRange(type: Int, start: Int, range: Int): MutableList<ScpModel> {
        val end = start + range
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? =
                        if (PreferenceUtil.getIfHideFinished()) {
                            this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                                    + ScpTable.SCP_TYPE + "=? AND " + ScpTable.HAS_READ + " = 0 ",
                                    arrayOf(type.toString()))
                        } else {
                            this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                                    + ScpTable.SCP_TYPE + "=?",
                                    arrayOf(type.toString()))
                        }
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
            }
            if (type == SERIES) {
                resultList.sortBy {
                    if (it.link.startsWith("/scp") && !it.link.split("-").isEmpty())
                        it.link.split("-")[1].toInt()
                    else it.link.substring(1, 5).toInt()
                }
            } else {
                resultList.sortBy {
                    it.link.split("-")[2].toInt()
                }
            }
            return resultList.subList(start, if (end < resultList.size) end else resultList.size - 1)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun getScpByType(type: Int): MutableList<ScpModel> {
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.SCP_TYPE + "=?;",
                        arrayOf(type.toString()))
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

    fun getTaleByTypeAndLetter(type: Int, pageCode: String): MutableList<ScpModel> {
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.SCP_TYPE + "=? AND " + ScpTable.PAGE_CODE + " = ?;",
                        arrayOf(type.toString(), pageCode))
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

    fun getSinglePageByType(type: Int): MutableList<ScpModel> {
        val abnormalPageList = arrayOf("/log-of-extranormal-events", "/log-of-extranormal-events-cn",
                "/log-of-anomalous-items", "/log-of-anomalous-items-cn")
        val introPageList = arrayOf("/faq", "/guide-for-newbies", "/how-to-write-an-scp")
        val infoPageList = arrayOf("/secure-facilities-locations", "/secure-facilities-locations-cn",
                "/object-classes", "/security-clearance-levels", "/task-forces")
        var resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.SCP_TYPE + "=" + SCPConstants.ScpType.SINGLE_PAGE, null)
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
            }
            resultList = resultList.filter {
                when (type) {
                    SCPConstants.ScpType.SAVE_INFO -> infoPageList.contains(it.link)
                    SCPConstants.ScpType.SAVE_INTRO -> introPageList.contains(it.link)
                    SCPConstants.ScpType.SAVE_ABNORMAL -> abnormalPageList.contains(it.link)
                    else -> abnormalPageList.contains(it.link)
                }
            }.toMutableList()

            return resultList

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }


    fun getDetailById(id: String): String {
        try {
            var detailString = ""
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT " + ScpTable.DETAIL_HTML
                        + " FROM " + ScpTable.DETAIL_TABLE_NAME + " WHERE "
                        + ScpTable.ID + "=? ", arrayOf(id))
                cursor?.let {
                    with(it) {
                        if (cursor.moveToFirst()) {
                            detailString = getCursorString(cursor, ScpTable.DETAIL_HTML)
                        }
                    }
                }
            }
            return detailString.replace("href=\"/",
                    "href=\"http://scp-wiki-cn.wikidot.com/")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getDetailByLink(link: String): String {
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT " + ScpTable.DETAIL_HTML
                    + " FROM " + ScpTable.DETAIL_TABLE_NAME + " WHERE "
                    + ScpTable.LINK + "=? ", arrayOf(link))
            var detailString = ""
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    detailString = getCursorString(cursor, ScpTable.DETAIL_HTML)
                }
                cursor.close()
            }
            return detailString.replace("href=\"/", "href=\"http://scp-wiki-cn.wikidot.com/")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun searchScpByKeyword(keyword: String): MutableList<ScpModel> {
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.TITLE + " LIKE ?;",
                        arrayOf("%$keyword%"))
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

    fun searchScpInDetailByKeyword(keyword: String): MutableList<ScpModel> {
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM "
                        + ScpTable.SCP_TABLE_NAME + " as scp left join " + ScpTable.DETAIL_TABLE_NAME + " as detail on " +
                        "scp.link = detail.link WHERE detail.detailHtml LIKE ?;",
                        arrayOf("%$keyword%"))
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

    fun getRandomScp(typeRange: String = ""): ScpModel? {
        randomCount++
        if (randomCount > 20) {
            randomCount = 0
            return null
        }
        var scpModel: ScpModel? = null
        var link = ""
        try {
            with(readableDatabase) {
                var cursor: Cursor? = null
                if (typeRange.isEmpty()) {
                    cursor = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.DETAIL_TABLE_NAME, null)
                } else {
                    cursor = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME +
                            " WHERE " + ScpTable.SCP_TYPE + " IN ($typeRange);", null)
                }
                if (cursor != null && cursor.count > 0) {
                    val randomIndex = Random().nextInt(cursor.count)
                    while (cursor.moveToNext()) {
                        if (cursor.position == randomIndex) {
                            link = getCursorString(cursor, ScpTable.LINK)
                            Log.i("random", link)
                            cursor.close()
                            break
                        }
                    }
                }
            }
            val detailHtml = getDetailByLink(link)
            scpModel = if (detailHtml.contains("抱歉，该页面尚无内容") || detailHtml.isEmpty())
                getRandomScp(typeRange) else getOneScpModelByLink(link)
            return scpModel
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun getNextScp(index: Int): ScpModel? {
        var scpModel: ScpModel? = null
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                    + ScpTable.INDEX + "=? ", arrayOf("${index + 1}"))
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    scpModel = extractScp(cursor)
                }
                cursor.close()
            }
            return scpModel
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scpModel
    }

    fun getPreviewScp(index: Int): ScpModel? {
        var scpModel: ScpModel? = null
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                    + ScpTable.INDEX + "=? ", arrayOf("${index - 1}"))
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    scpModel = extractScp(cursor)
                }
                cursor.close()
            }
            return scpModel
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return scpModel
    }

    fun getScpByTypeAndNumber(type: Int, number: String): ScpModel? {
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.SCP_TABLE_NAME + " WHERE "
                        + ScpTable.SCP_TYPE + "=? AND " + ScpTable.TITLE + " LIKE ?;",
                        arrayOf(type.toString(), if (type == SAVE_JOKE || type == SAVE_JOKE_CN)
                            "%-$number-%" else "%-$number%"))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            return extractScp(it)
                        }
                    }
                }
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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

    fun deleteDetailByDownloadType(downloadType: Int) {
        with(writableDatabase) {
            this.delete(ScpTable.DETAIL_TABLE_NAME, ScpTable.DOWNLOAD_TYPE + "=?",
                    arrayOf(downloadType.toString()))
        }
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

    private fun getCursorLong(cursor: Cursor?, columnIndex: String): Long {
        return if (cursor?.getColumnIndex(columnIndex)?.compareTo(0) == 1) {
            cursor.getLong(cursor.getColumnIndex(columnIndex))
        } else {
            0
        }
    }

    /**
     * model转cv存储
     */
    private fun packScp(model: ScpModel): ContentValues {
        val cv = ContentValues()
        if (model.sId.isNotEmpty()) {
            cv.put(ScpTable.ID, model.sId)
        }
        if (model.index > -1) {
            cv.put(ScpTable.INDEX, model.index)
        }
        if (model.link.isNotEmpty()) {
            cv.put(ScpTable.LINK, model.link)
        }
        if (model.title.isNotEmpty()) {
            cv.put(ScpTable.TITLE, model.title)
        }
        if (model.scpType > -1) {
            cv.put(ScpTable.SCP_TYPE, model.scpType)
        }
        if (model.downloadType > -1) {
            cv.put(ScpTable.DOWNLOAD_TYPE, model.downloadType)
        }

        if (model.subtext.isNotEmpty()) {
            cv.put(ScpTable.SUB_TEXT, model.subtext)
        }
        if (model.snippet.isNotEmpty()) {
            cv.put(ScpTable.SNIPPET, model.snippet)
        }
        if (model.desc.isNotEmpty()) {
            cv.put(ScpTable.DESC, model.desc)
        }
        if (model.author.isNotEmpty()) {
            cv.put(ScpTable.AUTHOR, model.author)
        }
        if (model.creator.isNotEmpty()) {
            cv.put(ScpTable.CREATOR, model.creator)
        }
        if (model.createdTime.isNotEmpty()) {
            cv.put(ScpTable.CREATED_TIME, model.createdTime)
        }

        if (model.tags.isNotEmpty()) {
            cv.put(ScpTable.TAGS, model.tags)
        }
        if (model.subLinks.isNotEmpty()) {
            cv.put(ScpTable.SUB_LINKS, model.subLinks)
        }

        cv.put(ScpTable.HAS_READ, model.hasRead)
        cv.put(ScpTable.LIKE, model.like)
        return cv
    }

    /**
     * cursor转model取出
     */
    private fun extractScp(cursor: Cursor): ScpModel {
        randomCount = 0
        return ScpModel(getCursorString(cursor, ScpTable.ID),
                getCursorInt(cursor, ScpTable.INDEX),
                "", "",
                getCursorString(cursor, ScpTable.LINK),
                getCursorString(cursor, ScpTable.TITLE),
                // 正文数据量太大，先不取出来，点击时再从数据库拿
                getCursorInt(cursor, ScpTable.SCP_TYPE),
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
