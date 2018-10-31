package info.free.scp.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.umeng.analytics.MobclickAgent
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel
import info.free.scp.util.PreferenceUtil
import info.free.scp.util.Toaster
import java.util.*


/**
 * Created by zhufree on 2018/8/27.
 * 数据库管理
 * 每次更新数据源时要删掉表重建
 * 所以已读数据和收藏需要单独建表存，以link为主键（存储时主键可能重复需要注意），存储时更新到总表中
 */


const val DB_NAME = "scp_info.db"
const val DB_VERSION = 3

class ScpDao : SQLiteOpenHelper(ScpApplication.context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ScpTable.CREATE_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_LIKE_AND_READ_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL(ScpTable.dropScpTableSQL)
            db?.execSQL(ScpTable.dropDetailTableSQL)
            db?.execSQL(ScpTable.CREATE_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_LIKE_AND_READ_TABLE_SQL)
        }
    }

    fun resetDb() {
        PreferenceUtil.setInitCategoryFinish(false)
        PreferenceUtil.resetDetailDataLoadCount()
        with(writableDatabase) {
            this?.execSQL(ScpTable.dropScpTableSQL)
            this?.execSQL(ScpTable.dropDetailTableSQL)
            // 先这么处理，回头改成添加字段啥的更新
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
        val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.LIKE_AND_READ_TABLE_NAME, null)
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
        val stmt = compileStatement(ScpTable.INSERT_SCP_SQL)
        for (model in models) {
            stmt.bindString(1, model.sId)
            stmt.bindString(2, model.link)
            stmt.bindString(3, model.title)
            stmt.bindString(4, model.detailHtml)
            stmt.bindLong(5, model.hasRead.toLong())
            stmt.bindString(6, model.saveType)
            stmt.bindString(7, model.subtext)
            stmt.bindString(8, model.snippet)
            stmt.bindString(9, model.desc)
            stmt.bindString(10, model.author)
            stmt.bindString(11, model.number)
            stmt.bindString(12, model.storyNum)
            stmt.bindString(13, model.pageCode)
            stmt.bindString(14, model.contestName)
            stmt.bindString(15, model.contestLink)
            stmt.bindString(16, model.createdTime)
            stmt.bindLong(17, model.index.toLong())
            stmt.bindString(18, model.evenType)
            stmt.bindString(19, model.month)
            stmt.bindString(21, model.notFound)
            Log.i("loading", "sid = ${model.sId}")
            stmt.execute()
            stmt.clearBindings()
        }
    }

    private fun SQLiteDatabase.createDetailStatement(models: List<ScpModel>) {
        try {
            val stmt = compileStatement(ScpTable.INSERT_DETAIL_SQL)
            for (model in models) {
                stmt.bindString(1, model.sId)
                stmt.bindString(2, model.detailHtml)
                Log.i("loading", "sid = ${model.sId}")
                stmt.execute()
                stmt.clearBindings()
            }
        } catch (e: Exception) {
            Log.i("detail", e.message)
            MobclickAgent.reportError(ScpApplication.context, e.message)
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
     * 网络请求到数据时保存一次
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
                db.insert(ScpTable.TABLE_NAME, null, cv)
            } else {
                // 如果之前有了，更新字段，阅读的信息只有新数据> 0时才会更新，所以不会覆盖
                db.update(ScpTable.TABLE_NAME, cv, ScpTable.ID + "=?",
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
    private fun getScpModelByLink(link: String?): MutableList<ScpModel?> {
        val resultList = emptyList<ScpModel?>().toMutableList()
        if (link == null) {
            return resultList
        }
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                        + ScpTable.LINK + "=?", arrayOf(link))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
                return resultList
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    /**
     * 收藏列表只展示一个
     */
    private fun getOneScpModelByLink(link: String): ScpModel? {
        return getScpModelByLink(link)[0]
    }

    fun getScpModelById(id: String?): ScpModel? {
        if (id == null) {
            return null
        }
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                        + ScpTable.ID + "=?", arrayOf(id))
                var scpModel: ScpModel? = null
                with(cursor) {
                    this?.let {
                        if (it.moveToFirst()) {
                            scpModel = extractScp(it)
                        }
                    }
                }
                return scpModel
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getScpByTypeAndRange(type: String, start: Int, range: Int) : MutableList<ScpModel>{
        val end = start + range
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                        + ScpTable.SAVE_TYPE + "=? ",
                        arrayOf(type))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
                return resultList.subList(start, if (end < resultList.size) end else resultList.size-1)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun getScpByType(type: String) : MutableList<ScpModel>{
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                        + ScpTable.SAVE_TYPE + "=?;",
                        arrayOf(type))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
                return resultList
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun getDetailById(id: String): String {
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT " + ScpTable.DETAIL_HTML
                        + " FROM " + ScpTable.DETAIL_TABLE_NAME + " WHERE "
                        + ScpTable.ID + "=? ", arrayOf(id))
                var detailString = ""
                cursor?.let {
                    with(it) {
                        if (cursor.moveToFirst()) {
                            detailString = getCursorString(cursor, ScpTable.DETAIL_HTML)
                        }
                    }
                }
                return detailString.replace("href=\"/",
                        "href=\"http://scp-wiki-cn.wikidot.com/")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getDetailByLink(link: String): String {
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT " + ScpTable.ID
                    + " FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.LINK + "=? ", arrayOf(link))
            var detailString = ""
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val id = getCursorString(cursor, ScpTable.ID)
                    detailString = getDetailById(id)
                }
                cursor.close()
            }
            return detailString.replace("href=\"/", "href=\"http://scp-wiki-cn.wikidot.com/")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun searchScpByKeyword(keyword: String): MutableList<ScpModel>{
        val resultList = emptyList<ScpModel>().toMutableList()
        try {
            with(readableDatabase) {
                val cursor: Cursor? = this.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                        + ScpTable.TITLE + " LIKE ?;",
                        arrayOf("%$keyword%"))
                with(cursor) {
                    this?.let {
                        while (it.moveToNext()) {
                            resultList.add(extractScp(it))
                        }
                    }
                }
                return resultList
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun getRandomScp(): ScpModel? {
        val randomIndex = Random().nextInt(14002)
        var scpModel: ScpModel? = null
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.INDEX + "=? ", arrayOf(randomIndex.toString()))
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val sId = getCursorString(cursor, ScpTable.ID)
                    Log.i("random", sId)
                    val detailHtml = getDetailById(sId)
                    scpModel = if (detailHtml.contains("抱歉，该页面尚无内容") || detailHtml.isEmpty()) getRandomScp() else extractScp(cursor)
                }
                cursor.close()
            }
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
                return resultList
            }

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
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.INDEX + "=? ", arrayOf("${index+1}"))
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
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.INDEX + "=? ", arrayOf("${index-1}"))
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

    companion object {
        private var scpDao: ScpDao? = ScpDao()
        fun getInstance(): ScpDao {
            if (scpDao == null) {
                scpDao = ScpDao()
            }
            return scpDao!!
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
     * model转cv存储
     */
    private fun packScp(model: ScpModel): ContentValues {
        val cv = ContentValues()
        if (model.sId.isNotEmpty()) {
            cv.put(ScpTable.ID, model.sId)
        }
        if (model.link.isNotEmpty()) {
            cv.put(ScpTable.LINK, model.link)
        }
        if (model.title.isNotEmpty()) {
            cv.put(ScpTable.TITLE, model.title)
        }
        if (model.detailHtml.isNotEmpty()) {
            cv.put(ScpTable.DETAIL_HTML, model.detailHtml)
        }
        cv.put(ScpTable.HAS_READ, model.hasRead)
        cv.put(ScpTable.LIKE, model.like)

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
        if (model.createdTime.isNotEmpty()) {
            cv.put(ScpTable.CREATED_TIME, model.createdTime)
        }
        if (model.number.isNotEmpty()) {
            cv.put(ScpTable.NUMBER, model.number)
        }
        if (model.storyNum.isNotEmpty()) {
            cv.put(ScpTable.STORY_NUMBER, model.storyNum)
        }
        if (model.pageCode.isNotEmpty()) {
            cv.put(ScpTable.PAGE_CODE, model.pageCode)
        }
        if (model.contestName.isNotEmpty()) {
            cv.put(ScpTable.CONTEST_NAME, model.contestName)
        }
        if (model.contestLink.isNotEmpty()) {
            cv.put(ScpTable.CONTEST_LINK, model.contestLink)
        }
        if (model.saveType.isNotEmpty()) {
            cv.put(ScpTable.SAVE_TYPE, model.saveType)
        }
        if (model.index > -1) {
            cv.put(ScpTable.INDEX, model.index)
        }
        if (model.evenType.isNotEmpty()) {
            cv.put(ScpTable.EVENT_TYPE, model.evenType)
        }
        if (model.month.isNotEmpty()) {
            cv.put(ScpTable.MONTH, model.month)
        }
        if (model.notFound.isNotEmpty()) {
            cv.put(ScpTable.NOT_FOUND, model.notFound)
        }
        return cv
    }

    /**
     * cursor转model取出
     */
    private fun extractScp(cursor: Cursor): ScpModel {
        return ScpModel(getCursorString(cursor, ScpTable.ID),
                "", "",
                getCursorString(cursor, ScpTable.LINK),
                getCursorString(cursor, ScpTable.TITLE), "",
                getCursorString(cursor, ScpTable.NOT_FOUND),
                // 正文数据量太大，先不取出来，点击时再从数据库拿
                // getCursorString(cursor, ScpTable.DETAIL_HTML),
                getCursorInt(cursor, ScpTable.HAS_READ),
                getCursorInt(cursor, ScpTable.LIKE),
                getCursorString(cursor, ScpTable.SAVE_TYPE),
                getCursorString(cursor, ScpTable.AUTHOR),
                getCursorString(cursor, ScpTable.SUB_TEXT),
                getCursorString(cursor, ScpTable.SNIPPET),
                getCursorString(cursor, ScpTable.DESC),
                getCursorString(cursor, ScpTable.NUMBER),
                getCursorString(cursor, ScpTable.STORY_NUMBER),
                getCursorInt(cursor, ScpTable.INDEX),
                getCursorString(cursor, ScpTable.PAGE_CODE),
                getCursorString(cursor, ScpTable.CREATED_TIME),
                getCursorString(cursor, ScpTable.CONTEST_NAME),
                getCursorString(cursor, ScpTable.CONTEST_LINK),
                "", "",
                getCursorString(cursor, ScpTable.EVENT_TYPE),
                getCursorString(cursor, ScpTable.MONTH)
        )

    }
}
