package info.free.scp.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import info.free.scp.SCPConstants.SAVE_ABOUT
import info.free.scp.ScpApplication
import info.free.scp.bean.ScpModel
import java.util.*


/**
 * Created by zhufree on 2018/8/27.
 * 数据库管理
 */


const val DB_NAME = "scp_info.db"
const val DB_VERSION = 1

class ScpDao : SQLiteOpenHelper(ScpApplication.context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ScpTable.CREATE_TABLE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL(ScpTable.dropScpTableSQL)
            // 先这么处理，回头改成添加字段啥的更新
            db?.execSQL(ScpTable.CREATE_TABLE_SQL)
            //检测新加的表字段是否存在，如果不存在的话添加Alter
            val columnNameList = getColumnNameList(db, ScpTable.TABLE_NAME)
//            if (columnNameList != null) {
//            //先暂时使用这种方式进行判断
//                if (!columnNameList.contains(ScpTable.NUMBER)) {
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

    fun initBasicInfo() {
        val basicScp = ScpModel("","", "",
                "/security-clearance-levels", "安保许可等级", "", 0,
                SAVE_ABOUT, "", "", "",
                "", "", "", 0, "", "",
                "","","about", "")
        replaceScpModel(basicScp)
        basicScp.link = "/object-classes"
        basicScp.title = "项目分级"
        basicScp.index = 1
        replaceScpModel(basicScp)
        basicScp.link = "/secure-facilities-locations"
        basicScp.title = "安保设施地点"
        basicScp.index = 2
        replaceScpModel(basicScp)
        basicScp.link = "/task-forces"
        basicScp.title = "机动特遣队"
        basicScp.index = 3
        replaceScpModel(basicScp)
        basicScp.link = "/log-of-anomalous-items"
        basicScp.title = "异常项目记录"
        basicScp.index = 4
        replaceScpModel(basicScp)
        basicScp.link = "/log-of-extranormal-events"
        basicScp.title = "超常事件记录"
        basicScp.index = 5
        replaceScpModel(basicScp)
        basicScp.link = "/secure-facilities-locations-cn"
        basicScp.title = "安保设施地点(CN)"
        basicScp.index = 6
        replaceScpModel(basicScp)
    }

    fun getBasicInfo(): MutableList<ScpModel> {
        val basicList = emptyList<ScpModel>().toMutableList()
        getScpModelByLink("/security-clearance-levels")?.let {
            basicList.add(it)
        }
        getScpModelByLink("/object-classes")?.let {
            basicList.add(it)
        }
        getScpModelByLink("/secure-facilities-locations")?.let {
            basicList.add(it)
        }
        getScpModelByLink("/task-forces")?.let {
            basicList.add(it)
        }
        getScpModelByLink("/log-of-anomalous-items")?.let {
            basicList.add(it)
        }
        getScpModelByLink("/log-of-extranormal-events")?.let {
            basicList.add(it)
        }
        getScpModelByLink("/secure-facilities-locations-cn")?.let {
            basicList.add(it)
        }
        return basicList
    }

    fun insertData(models: List<ScpModel>) {
        if (models.isEmpty()) {
            return
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
//            for (model in models) {
//                val cv = packScp(model)
//                if (getScpModelByLink(model.sId) == null) {
//
//                    // 如果之前没有，直接存储
//                    db.insert(ScpTable.TABLE_NAME, null, cv)
//                } else {
//                    // 如果之前有了，更新字段，阅读的信息只有新数据> 0时才会更新，所以不会覆盖
//                    db.update(ScpTable.TABLE_NAME, cv, ScpTable.ID + "=?",
//                            arrayOf(model.sId))
//                }
//            }
            db.createStatement(models)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun SQLiteDatabase.createStatement(models: List<ScpModel>) {
        val stmt = compileStatement(ScpTable.INSERT_SQL)
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
        return cv
    }

    /**
     * cursor转model取出
     */
    private fun extractScp(cursor: Cursor): ScpModel {
        return ScpModel(getCursorString(cursor, ScpTable.ID),
                "", "",
                getCursorString(cursor, ScpTable.LINK),
                getCursorString(cursor, ScpTable.TITLE),
                getCursorString(cursor, ScpTable.DETAIL_HTML),
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
                "",
                ""
        )

    }
}
