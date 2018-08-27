package info.free.scp.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel
import java.util.*

/**
 * Created by zhufree on 2018/8/27.
 * 数据库管理
 */


const val DB_NAME = "shelf_info.db"
const val DB_VERSION = 1

class ScpDao : SQLiteOpenHelper(ScpApplication.context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ScpTable.CREATE_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            //            db?.execSQL(ScpTable.DropScpTableSQL)
            // 先这么处理，回头改成添加字段啥的更新
            db?.execSQL(ScpTable.CREATE_TABLE_SQL)
            //检测新加的表字段是否存在，如果不存在的话添加Alter
            val columnNameList = getColumnNameList(db, ScpTable.TABLE_NAME)
            //            if (columnNameList != null) {
            //先暂时使用这种方式进行判断
            //                if (!columnNameList.contains(ScpTable.FIRST_ARTICLE_ID)) {
            //                    val sql = createAddAlterSql(ScpTable.TABLE_NAME, "${ScpTable.FIRST_ARTICLE_ID} INTEGER default -1")
            //                    if (!Tools.StringIsEmpty(sql)) {
            //                        db?.execSQL(sql)
            //                    }
            //                }
            //            }
        }
    }

    private fun getColumnNameList(db: SQLiteDatabase?, tableName: String): ArrayList<String> {
        val sql = "PRAGMA table_info('$tableName');"
        var cursor: Cursor? = null
        val columnNameList = ArrayList<String>()
        db?.use {
            cursor = it.rawQuery(sql, null)
            cursor?.use {
                val columnNames = cursor?.columnNames
                val count = cursor?.columnCount
                while (cursor?.moveToNext() == true) {
                    val columnName = getCursorString(cursor, "name")
                    if (columnName.isNotEmpty()) {
                        columnNameList.add(columnName)
                    }
                }
            }
        }

        return columnNameList
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
            if (getShelfInfoByLink(model.link) == null) {
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
        return getShelfInfoByLink(model.link)
    }

    fun getShelfInfoByLink(link: String?): ScpModel? {
        if (link == null) {
            return null
        }
        try {
            val cursor: Cursor? = readableDatabase.rawQuery("SELECT * FROM " + ScpTable.TABLE_NAME + " WHERE "
                    + ScpTable.LINK + "=?", arrayOf(link))
            var shelfModel: ScpModel? = null
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    shelfModel = extractScp(cursor)
                }
                cursor.close()
            }
            return shelfModel

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getSubShelfInfoByName(name: String): ScpModel? {
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
        return if (cursor?.getColumnIndex(columnIndex)?.compareTo(0) == 1) {
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
        if (model.updatedTime.isNotEmpty()) {
            cv.put(ScpTable.UPDATED_TIME, model.updatedTime)
        }
        cv.put(ScpTable.HAS_READ, if (model.hasRead) 1 else 0)
        return cv
    }

    /**
     * cursor转model取出
     */
    private fun extractScp(cursor: Cursor): ScpModel {
        return ScpModel(getCursorString(cursor, ScpTable.ID),
                getCursorString(cursor, ScpTable.LINK),
                getCursorString(cursor, ScpTable.TITLE),
                getCursorString(cursor, ScpTable.DETAIL_HTML),
                getCursorString(cursor, ScpTable.SUB_TEXT),
                getCursorString(cursor, ScpTable.SNIPPET),
                getCursorString(cursor, ScpTable.DESC),
                getCursorString(cursor, ScpTable.AUTHOR),
                getCursorString(cursor, ScpTable.CREATED_TIME),
                getCursorString(cursor, ScpTable.UPDATED_TIME),
                getCursorInt(cursor, ScpTable.HAS_READ) == 1,
                "", "")

    }
}
