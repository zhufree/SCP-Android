package info.free.scp

/**
 * Created by zhufree on 2018/8/22.
 *
 */

object SCPConstants {
    const val PACKAGE_NAME = "info.free.scp"
    // category type
    object Entry {
        const val SCP_DOC = 0 // SCP文档，包含其他文档
        const val STORY_DOC = 1 // 故事外围
        const val ABOUT_SCP_DOC = 3 // 关于基金会 设定中心，GOI、hub等，相关材料，艺作、传承、精品、竞赛、征文等
        const val JOKE_DOC = 4 // 搞笑作品
    }
    object Download {
        const val DOWNLOAD_SCP = 0
        const val DOWNLOAD_SCP_CN = 1
        const val DOWNLOAD_TALE = 2
        const val DOWNLOAD_ARCHIVES = 3
        const val DOWNLOAD_COLLECTIONS = 4
        const val DOWNLOAD_TOTAL = DOWNLOAD_COLLECTIONS
    }
    object Category {
        const val SERIES = 1
        const val SERIES_CN = 2
        const val SCP_EX = 3
        const val SCP_EX_CN = 4
        const val SCP_ARCHIVES = 17
        const val SCP_REMOVED = 18
        const val SCP_DECOMMISSIONED = 19
        const val SCP_ABNORMAL = 20
        const val ABOUT_INFO = 5
        const val ABOUT_INTRO = 16
        const val TALES = 6
        const val TALES_CN = 7
        const val STORY_SERIES = 8
        const val STORY_SERIES_CN = 9
        const val JOKE = 21
        const val JOKE_CN = 22
        const val SETTINGS = 10
        const val SETTINGS_CN = 11
        const val CONTEST = 12
        const val CONTEST_CN = 13
        const val EVENT = 14
        const val TALES_BY_TIME = 15
    }

    object ScpType {
        // save type
        const val SINGLE_PAGE = 0 // 单页面
        const val SAVE_INFO = 102 // 设定说明类页面
        const val SAVE_ABNORMAL = 103 // 其中的异常物品超长事件等
        const val SAVE_INTRO = 104 // 其中 faq，新手指南之类

        const val SAVE_SERIES = 1
        const val SAVE_SERIES_CN = 2

        const val SAVE_TALES = 3
        const val SAVE_TALES_CN = 4
        const val SAVE_TALES_BY_TIME = 101

        const val SAVE_JOKE = 5
        const val SAVE_JOKE_CN = 6
        const val SAVE_ARCHIVED = 7
        const val SAVE_EX = 8
        const val SAVE_EX_CN = 9
        const val SAVE_DECOMMISSIONED = 10
        const val SAVE_REMOVED = 11
        // 后面加字母

        const val SAVE_STORY_SERIES = 19
        const val SAVE_STORY_SERIES_CN = 20
        const val SAVE_SETTINGS = 13
        const val SAVE_SETTINGS_CN = 14
        const val SAVE_CONTEST = 16
        const val SAVE_CONTEST_CN = 18


    }

    const val BMOB_API_URL = "https://api.bmob.cn/"
    const val SCP_SITE_URL = "http://scp-wiki-cn.wikidot.com"

    // 广播类型
    object BroadCastAction {
        const val INIT_PROGRESS = "initProgress"
        const val LOAD_DETAIL_FINISH = "loadDetailFinish"
        const val ACTION_CHANGE_THEME = "changeTheme"
    }

    // RequestCode
    object RequestCode {
        const val CATEGORY_TO_DETAIL = 0
    }

    const val HISTORY_TYPE = 0
    const val LATER_TYPE = 1
}