package info.free.scp

/**
 * Created by zhufree on 2018/8/22.
 *
 */

object SCPConstants {
    const val PACKAGE_NAME = "info.free.scp"
    const val SCP_DB_NAME = "scp_category_v2.db"
    const val DETAIL_DB_NAME = "scp_detail_v2.db"
    const val INFO_DB_NAME = "scp_info.db"
    const val LEVEL_PREF_NAME = "level.xml"
    const val APP_PREF_NAME = "app.xml"
    const val PUBLIC_DIR_NAME = "scp_reader"
    const val FREE_TIME = 1619539200000

    object AppMode {
        const val ONLINE = 0
        const val OFFLINE = 1
    }

    object OrderType {
        const val ASC = 1
        const val DESC = -1
    }

    object SearchType {
        const val TITLE = 0
        const val CONTENT = 1
    }

    // category type
    object Entry {
        const val SCP_DOC = 1001 // SCP文档，包含其他文档
        const val SCP_CN_DOC = 1002 // SCP-CN文档，包含其他文档
        const val STORY_DOC = 1003 // 故事外围
        const val GOI_DOC = 1004 // 故事外围
        const val ART_DOC = 1005 // 故事外围
        const val WANDER_DOC = 1006 // 故事外围
        const val LIBRARY_DOC = 1007 // 故事外围
        const val INTERNATIONAL_DOC = 1008 // 故事外围
        const val INFORMATION_DOC = 1009 // 故事外围

    }


    object Category {
        // 直接跳转列表 100+
        const val SERIES = 1
        const val SERIES_CN = 2
        const val JOKE = 101
        const val SCP_EX = 102
        const val TALES = 99
        const val TALES_CN = 98
        const val TALES_BY_TIME = 97
        const val SETTINGS = 106
        const val SETTINGS_CN = 107
        const val STORY_SERIES = 108
        const val STORY_SERIES_CN = 109
        const val CONTEST = 110
        const val CONTEST_CN = 111
        const val WANDER = 10
        const val WANDER_CN = 11
    }

    object ScpType {
        // save type
        const val SAVE_SERIES = 1
        const val SAVE_SERIES_CN = 2
        const val SAVE_JOKE = 3
        const val SAVE_JOKE_CN = 4
        const val SAVE_EX = 5
        const val SAVE_EX_CN = 6

        const val SAVE_TALES = 7
        const val SAVE_TALES_CN = 8
        const val SAVE_CANON = 9
        const val SAVE_CANON_CN = 10
        const val SAVE_STORY_SERIES = 11
        const val SAVE_STORY_SERIES_CN = 12

        const val SAVE_REPORT = 13
        const val SAVE_ANOMALOUS_CN = 14
        const val SAVE_SHORT_STORY = 15

        const val SAVE_LIBRARY_PAGE = 16
        const val SAVE_GOI = 17
        const val SAVE_ART = 18

        const val SAVE_CONTEST = 19
        const val SAVE_CONTEST_CN = 20
        const val SAVE_WANDER = 21
        const val SAVE_WANDER_CN = 22
        const val SAVE_INTERNATIONAL = 23
        const val SAVE_INFO_PAGE = 24 // 设定说明类页面

        const val SAVE_TALES_BY_TIME = 101
        const val SAVE_OFFSET = 100
    }

    const val BMOB_API_URL = "https://api.bmob.cn/"
    const val SCP_SITE_URL = "http://scp-wiki-cn.wikidot.com"

    // https://portal.mcseekeri.top/http/scp-wiki-cn.wikidot.com
    const val FEED_API_URL = "http://api.zhufree.fun"

    // 广播类型
    object BroadCastAction {
        const val ACTION_CHANGE_THEME = "changeTheme"
    }

    // RequestCode
    object RequestCode {
        const val CATEGORY_TO_DETAIL = 0
        const val REQUEST_FILE_PERMISSION = 1
        const val REQUEST_PICTURE_DIR = 2
        const val REQUEST_PUBLIC_FILE = 3
        const val REQUEST_BACKUP_DIR = 4
        const val REQUEST_RESTORE_DIR = 5
    }

    const val HISTORY_TYPE = 0
    const val LATER_TYPE = 1

    const val SIMPLE = 0
    const val TRADITIONAL = 1

    const val LATEST_CREATED = -2
    const val LATEST_TRANSLATED = -3
    const val TOP_RATED_ALL = 0
    const val TOP_RATED_SCP = 1
    const val TOP_RATED_TALES = 2
    const val TOP_RATED_GOI = 3
    const val TOP_RATED_WANDERS = 4

}