package info.free.scp

/**
 * Created by zhufree on 2018/8/22.
 *
 */

object SCPConstants {
    const val PACKAGE_NAME = "info.free.scp"
    // category type
    object Entry {
        const val SCP_DOC = 0 // SCP文档
        const val OTHER_DOC = 1 // 其他文档
        const val STORY_DOC = 2 // 故事外围
        const val ABOUT_STUFF = 3 // 相关材料，全部单页面
        const val JOKE_DOC = 4 // 搞笑作品
        const val SETTINGS_DOC = 5 // 设定中心，GOI、hub等
        const val MORE_ABOUT = 6 // 艺作、传承、精品、竞赛、征文等
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
        const val SCP_EX = 16
        const val SCP_ARCHIVED = 17
        const val SCP_REMOVED = 18
        const val SCP_DECOMMISSIONED = 19
        const val SCP_ABNORMAL = 20
        const val ABOUT_STUFF = 5
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

    object SaveType {
        // save type
        const val SAVE_SERIES = "Series"
        const val SAVE_SERIES_CN = "SeriesCn"
        const val SAVE_SERIES_STORY_1 = "SeriesStory1"
        const val SAVE_SERIES_STORY_2 = "SeriesStory2"
        const val SAVE_SERIES_STORY_3 = "SeriesStory3"
        const val SAVE_JOKE = "joke"
        const val SAVE_JOKE_CN = "jokeCn"
        const val SAVE_EX = "ex"
        const val SAVE_ARCHIVED = "archived"
        const val SAVE_REMOVED = "removed"
        const val SAVE_DECOMMISSIONED = "decommissioned"
        const val SAVE_INFO = "information"
        const val SAVE_ABNORMAL = "abnormal"
        // 后面加字母
        const val SAVE_TALES_PREFIX = "tales_"
        const val SAVE_TALES_CN_PREFIX = "talesCn_"
        const val SAVE_STORY_SERIES = "storySeries"
        const val SAVE_STORY_SERIES_CN = "storySeriesCn"
        const val SAVE_SETTINGS = "settings"
        const val SAVE_SETTINGS_CN = "settingsCn"
        const val SAVE_CONTEST = "contest"
        const val SAVE_CONTEST_CN = "contestCn"
        const val SAVE_EVENT = "event"
        const val SAVE_TALES_BY_TIME = "talesByTime"
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
}