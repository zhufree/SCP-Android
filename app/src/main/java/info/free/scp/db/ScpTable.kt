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
    const val HAS_READ = "hasRead"
    const val NUMBER = "number"
    const val STORY_NUMBER = "storyNumber"
    const val PAGE_CODE = "pageCode"
    const val CONTEST_NAME = "contestName"
    const val CONTEST_LINK = "contestLink"
    const val SAVE_TYPE = "saveType"
    const val INDEX = "index_"

    const val CREATE_TABLE_SQL = "create table IF NOT EXISTS $TABLE_NAME (" +
            "$ID VARCHAR PRIMARY KEY, " +
            "$LINK VARCHAR, $TITLE VARCHAR, " +
            "$DETAIL_HTML VARCHAR," +
            "$HAS_READ INTEGER, $SAVE_TYPE STRING," +
            "$SUB_TEXT VARCHAR, $SNIPPET VARCHAR, $DESC VARCHAR, $AUTHOR VARCHAR, " +
            "$NUMBER VARCHAR, $STORY_NUMBER VARCHAR, " +
            "$PAGE_CODE VARCHAR, " +
            "$CONTEST_NAME VARCHAR, $CONTEST_LINK VARCHAR," +
            "$CREATED_TIME VARCHAR, " +
            "$INDEX INTEGER)"

    const val INSERT_SQL = "INSERT INTO $TABLE_NAME (" +
            "$ID , $LINK , $TITLE , $DETAIL_HTML ,$HAS_READ , $SAVE_TYPE," +
            "$SUB_TEXT , $SNIPPET , $DESC , $AUTHOR , $NUMBER , $STORY_NUMBER , " +
            "$PAGE_CODE , $CONTEST_NAME , $CONTEST_LINK , $CREATED_TIME , " +
            "$INDEX) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

    const val dropScpTableSQL = "DROP TABLE IF EXISTS $TABLE_NAME ; "
}