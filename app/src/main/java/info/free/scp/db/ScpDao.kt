package info.free.scp.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import info.free.scp.SCPConstants.SAVE_ABOUT
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel


/**
 * Created by zhufree on 2018/8/27.
 * 数据库管理
 * 每次更新数据源时要删掉表重建
 * 所以已读数据需要单独建表存
 */


const val DB_NAME = "scp_info.db"
const val DB_VERSION = 1

class ScpDao : SQLiteOpenHelper(ScpApplication.context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ScpTable.CREATE_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL(ScpTable.dropScpTableSQL)
            // 先这么处理，回头改成添加字段啥的更新
            db?.execSQL(ScpTable.CREATE_TABLE_SQL)
            db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
        }
    }

    fun resetDb() {
        val db = writableDatabase
        db?.execSQL(ScpTable.dropScpTableSQL)
        db?.execSQL(ScpTable.dropDetailTableSQL)
        // 先这么处理，回头改成添加字段啥的更新
        db?.execSQL(ScpTable.CREATE_TABLE_SQL)
        db?.execSQL(ScpTable.CREATE_DETAIL_TABLE_SQL)
    }

    fun insertCategoryData(models: List<ScpModel>) {
        if (models.isEmpty()) {
            return
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.createStatement(models)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
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
            Log.i("loading", "sid = ${model.sId}")
            stmt.execute()
            stmt.clearBindings()
        }
    }

    private fun SQLiteDatabase.createDetailStatement(models: List<ScpModel>) {
        val stmt = compileStatement(ScpTable.INSERT_DETAIL_SQL)
        for (model in models) {
            stmt.bindString(1, model.sId)
            stmt.bindString(2, model.detailHtml)
            Log.i("loading", "sid = ${model.sId}")
            stmt.execute()
            stmt.clearBindings()
        }
    }
    /**
     * 网络请求到数据时保存一次
     * 返回更新后的model
     */
    fun replaceScpModel(model: ScpModel?): ScpModel? {
        if (model == null || model.link.isEmpty()) {
            return null
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = packScp(model)
            if (getScpModelByLink(model.link) == null) {
                // 如果之前没有，直接存储
                db.insert(ScpTable.TABLE_NAME, null, cv)
            } else {
                // 如果之前有了，更新字段，阅读的信息只有新数据> 0时才会更新，所以不会覆盖
                db.update(ScpTable.TABLE_NAME, cv, ScpTable.LINK + "=?",
                        arrayOf(model.link))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return getScpModelByLink(model.link)
    }

    fun getScpModelByLink(link: String?): ScpModel? {
        if (link == null) {
            return null
        }
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.LINK + "=?", arrayOf(link))
            var scpModel: ScpModel? = null
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
        return null
    }
    fun getScpModelById(id: String?): ScpModel? {
        if (id == null) {
            return null
        }
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.ID + "=?", arrayOf(id))
            var scpModel: ScpModel? = null
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

    private fun getScpInfoByLink(name: String): ScpModel? {
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.LINK + "=? ", arrayOf(name))
            var shelfModel: ScpModel? = null
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    //                    shelfModel = extractScpModel(cursor)
                }
                cursor.close()
            }
            return shelfModel

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getDetailById(id: String): String {
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT " + ScpTable.DETAIL_HTML
                    + " FROM " + ScpTable.DETAIL_TABLE_NAME + " WHERE "
                    + ScpTable.ID + "=? ", arrayOf(id))
            var detailString = ""
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    detailString = getCursorString(cursor, ScpTable.DETAIL_HTML)
                }
                cursor.close()
            }
            return detailString
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
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
                // 正文数据量太大，先不取出来，点击时再从数据库拿
                // getCursorString(cursor, ScpTable.DETAIL_HTML),
                getCursorInt(cursor, ScpTable.HAS_READ),
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
