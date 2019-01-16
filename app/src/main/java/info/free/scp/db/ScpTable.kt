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
    const val INDEX = "index_"
    const val LINK = "link"
    const val TITLE = "title"
    const val DETAIL_HTML = "detailHtml"
    const val NOT_FOUND = "notFound"
    const val SCP_TYPE = "scpType"
    const val DOWNLOAD_TYPE = "downloadType"

    const val SUB_TEXT = "subtext"
    const val SNIPPET = "snippet"
    const val DESC = "desc"
    const val AUTHOR = "author"
    const val CREATOR = "creator"
    const val CREATED_TIME = "createdTime"
    const val PAGE_CODE = "pageCode"
    const val CONTEST_NAME = "contestName"
    const val CONTEST_LINK = "contestLink"
    const val EVENT_TYPE = "eventType"
    const val MONTH = "month"
    const val TAGS = "tags"
    const val SUB_LINKS = "subLinks"
    const val IS_COLLECTION = "isCollection"

    const val HAS_READ = "hasRead"
    const val LIKE = "like"

    const val CREATE_TABLE_SQL = "create table IF NOT EXISTS $TABLE_NAME (" +
            "$ID VARCHAR PRIMARY KEY, $INDEX INTEGER," +
            "$LINK VARCHAR, $TITLE VARCHAR, " +
            "$DETAIL_HTML TEXT, $NOT_FOUND INTEGER," +
            "$SCP_TYPE INTEGER, $DOWNLOAD_TYPE INTEGER," +
            "$SUB_TEXT VARCHAR, $SNIPPET VARCHAR, $DESC VARCHAR," +
            "$AUTHOR VARCHAR, $CREATOR VARCHAR" +
            "$CREATED_TIME VARCHAR, $PAGE_CODE VARCHAR, " +
            "$CONTEST_NAME VARCHAR, $CONTEST_LINK VARCHAR," +
            "$EVENT_TYPE VARCHAR, $MONTH VARCHAR, " +
            "$TAGS VARCHAR, $SUB_LINKS VARCHAR, $IS_COLLECTION INTEGER, " +
            "$LIKE INTEGER, $HAS_READ INTEGER" +
            ")"

    const val CREATE_DETAIL_TABLE_SQL = "create table IF NOT EXISTS $DETAIL_TABLE_NAME (" +
            "$LINK VARCHAR, $DETAIL_HTML VARCHAR, $DOWNLOAD_TYPE INTEGER)"

    const val CREATE_LIKE_AND_READ_TABLE_SQL = "create table IF NOT EXISTS $LIKE_AND_READ_TABLE_NAME (" +
            "$LINK VARCHAR PRIMARY KEY, $TITLE VARCHAR, $LIKE INTEGER, $HAS_READ INTEGER)"

    const val INSERT_SCP_SQL = "INSERT INTO $TABLE_NAME (" +
            "$ID,$INDEX, $LINK, $TITLE, $DETAIL_HTML, $NOT_FOUND, $SCP_TYPE, $DOWNLOAD_TYPE," +
            "$SUB_TEXT , $SNIPPET , $DESC , $AUTHOR, $CREATOR" +
            "$CREATED_TIME,$PAGE_CODE , $CONTEST_NAME , $CONTEST_LINK ,  " +
            "$EVENT_TYPE, $MONTH, $TAGS, $SUB_LINKS, $IS_COLLECTION, " +
            "$LIKE, $HAS_READ) " +
            "VALUES (?,?,?,?,?,?," +
            "?,?,?,?,?,?," +
            "?,?,?,?,?,?," +
            "?,?,?,?,?,?)"

    const val INSERT_DETAIL_SQL = "INSERT INTO $DETAIL_TABLE_NAME (" +
            "$LINK, $DETAIL_HTML, $DOWNLOAD_TYPE) VALUES (?,?,?)"

    const val INSERT_LIKE_SQL = "INSERT INTO $LIKE_AND_READ_TABLE_NAME (" +
            "$LINK, $TITLE, $LIKE, $HAS_READ) VALUES (?,?,?,?)"

    const val dropScpTableSQL = "DROP TABLE IF EXISTS $TABLE_NAME"

    const val dropDetailTableSQL = "DROP TABLE IF EXISTS $DETAIL_TABLE_NAME"

    const val dropLikeTableSQL = "DROP TABLE IF EXISTS $LIKE_AND_READ_TABLE_NAME"
}