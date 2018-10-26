package info.free.scp.db

/**
 * Created by zhufree on 2018/8/27.
 * 数据库表
 */

object ScpTable {
    const val TABLE_NAME = "ScpTable"
    const val DETAIL_TABLE_NAME = "DetailTable"
    const val LIKE_AND_READ_TABLE_NAME = "LikeAndReadTable"
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
    const val EVENT_TYPE = "eventType"
    const val MONTH = "month"

    const val LIKE = "like"

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
            "$INDEX INTEGER, " +
            "$EVENT_TYPE VARCHAR, $MONTH VARCHAR, " +
            "$LIKE INTEGER" +
            ")"

    const val CREATE_DETAIL_TABLE_SQL = "create table IF NOT EXISTS $DETAIL_TABLE_NAME (" +
            "$ID VARCHAR PRIMARY KEY, $DETAIL_HTML VARCHAR)"

    const val CREATE_LIKE_AND_READ_TABLE_SQL = "create table IF NOT EXISTS $LIKE_AND_READ_TABLE_NAME (" +
            "$LINK VARCHAR PRIMARY KEY, $TITLE VARCHAR, $LIKE INTEGER, $HAS_READ INTEGER)"

    const val INSERT_SCP_SQL = "INSERT INTO $TABLE_NAME (" +
            "$ID , $LINK , $TITLE , $DETAIL_HTML ,$HAS_READ , $SAVE_TYPE," +
            "$SUB_TEXT , $SNIPPET , $DESC , $AUTHOR , $NUMBER , $STORY_NUMBER , " +
            "$PAGE_CODE , $CONTEST_NAME , $CONTEST_LINK , $CREATED_TIME , " +
            "$INDEX, $EVENT_TYPE, $MONTH, $LIKE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

    const val INSERT_DETAIL_SQL = "INSERT INTO $DETAIL_TABLE_NAME (" +
            "$ID, $DETAIL_HTML) VALUES (?,?)"

    const val INSERT_LIKE_SQL = "INSERT INTO $LIKE_AND_READ_TABLE_NAME (" +
            "$LINK, $TITLE, $LIKE, $HAS_READ) VALUES (?,?,?,?)"

    const val dropScpTableSQL = "DROP TABLE IF EXISTS $TABLE_NAME"

    const val dropDetailTableSQL = "DROP TABLE IF EXISTS $DETAIL_TABLE_NAME"

    const val dropLikeTableSQL = "DROP TABLE IF EXISTS $LIKE_AND_READ_TABLE_NAME"
}