package info.free.scp.db

/**
 * Created by zhufree on 2018/8/27.
 * 数据库表
 */

object ScpTable {
    const val TABLE_NAME = "ScpTable"
    const val ID = "sId"
    const val LINK = "link"
    const val TITLE = "title"
    const val DETAIL_HTML = "detailHtml"
    const val SUB_TEXT = "subtext"
    const val SNIPPET = "snippet"
    const val DESC = "desc"
    const val AUTHOR = "author"
    const val CREATED_TIME = "createdTime"
    const val UPDATED_TIME = "updatedTime"
    const val HAS_READ = "hasRead"
    const val NUMBER = "number"

    const val CREATE_TABLE_SQL = "create table IF NOT EXISTS $TABLE_NAME (" +
            "$ID VARCHAR, $LINK VARCHAR PRIMARY KEY, $TITLE VARCHAR, $DETAIL_HTML VARCHAR," +
            "$SUB_TEXT VARCHAR, $SNIPPET VARCHAR, $DESC VARCHAR, $AUTHOR VARCHAR, " +
            "$CREATED_TIME VARCHAR, $UPDATED_TIME VARCHAR, $HAS_READ INTEGER, $NUMBER VARCHAR)"
}