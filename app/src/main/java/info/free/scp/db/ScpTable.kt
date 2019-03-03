package info.free.scp.db

/**
 * Created by zhufree on 2018/8/27.
 * 数据库表
 */

object ScpTable {
    const val SCP_TABLE_NAME = "ScpTable" // 目录表
    const val DETAIL_TABLE_NAME = "DetailTable" // 正文表
    const val LIKE_AND_READ_TABLE_NAME = "LikeAndReadTable" // 收藏和读过表
    const val VIEW_LIST_TABLE_NAME = "LaterAndHistoryTable" // 待读列表和历史阅读记录表

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

    // 读过历史和待读列表，有可能某个scp在两个列表都存在？
    // 应该不会，在阅读过时就应该从待读里面删除，在历史里面添加
    const val VIEW_TIME = "viewTime" // 上次阅读的历史时间/加入待读列表的时间,根据type判断
    const val VIEW_LIST_TYPE = "viewListType" // 待读还是读过的type，用一个表

    const val CREATE_TABLE_SQL = "create table IF NOT EXISTS $SCP_TABLE_NAME (" +
            "$ID VARCHAR PRIMARY KEY, $INDEX INTEGER," +
            "$LINK VARCHAR, $TITLE VARCHAR, " +
            "$DETAIL_HTML TEXT, $NOT_FOUND INTEGER," +
            "$SCP_TYPE INTEGER, $DOWNLOAD_TYPE INTEGER," +
            "$SUB_TEXT VARCHAR, $SNIPPET VARCHAR, $DESC VARCHAR," +
            "$AUTHOR VARCHAR, $CREATOR VARCHAR," +
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

    const val CREATE_VIEW_LIST_TABLE_SQL = "create table IF NOT EXISTS $VIEW_LIST_TABLE_NAME (" +
            "$LINK VARCHAR PRIMARY KEY, $TITLE VARCHAR, $VIEW_LIST_TYPE INTEGER, " +
            // timestamp DEFAULT CURRENT_TIMESTAMP，在初始化时自动设置时间戳
            "$VIEW_TIME TIMESTAMP NOT NULL DEFAULT (datetime('now','localtime'))" +
            ")"

    const val INSERT_SCP_SQL = "INSERT INTO $SCP_TABLE_NAME (" +
            "$ID,$INDEX, $LINK, $TITLE, $DETAIL_HTML, $NOT_FOUND, $SCP_TYPE, $DOWNLOAD_TYPE," +
            "$SUB_TEXT , $SNIPPET , $DESC, $AUTHOR, $CREATOR, " +
            "$CREATED_TIME,$PAGE_CODE , $CONTEST_NAME , $CONTEST_LINK," +
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

    const val INSERT_VIEW_LIST_SQL = "REPLACE INTO $VIEW_LIST_TABLE_NAME (" +
            "$LINK, $TITLE, $VIEW_LIST_TYPE) VALUES (?,?,?)"

    const val dropScpTableSQL = "DROP TABLE IF EXISTS $SCP_TABLE_NAME"

    const val dropDetailTableSQL = "DROP TABLE IF EXISTS $DETAIL_TABLE_NAME"

    const val dropLikeTableSQL = "DROP TABLE IF EXISTS $LIKE_AND_READ_TABLE_NAME"
}