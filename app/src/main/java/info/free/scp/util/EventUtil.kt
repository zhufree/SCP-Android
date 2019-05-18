package info.free.scp.util

import android.content.Context
import com.tendcloud.tenddata.TCAgent
import com.umeng.analytics.MobclickAgent
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/9/21.
read_detail, 阅读文档, 1
finish_read, 标记已读, 1
cancel_has_read, 取消已读的文档, 1

copy_link, 复制文档链接 , 0
open_in_browser, 在浏览器打开文档, 0
like, 收藏文档, 0

choose_job, 选择基金会职业, 0

click_donation, 点击捐赠开发者, 1
random_all, 随机全部, 1
random_scp, 随机scp, 1
random_tale, 随机故事, 1
random_joke, 随机joke, 1
direct, 直达文档, 1
later_read_list, 稍后阅读列表, 1
add_later_read, 添加稍后阅读
read_history_list, 阅读历史, 1
import_read_list, 导入待读列表, 1
draft, 草稿, 1
add_widget, 添加了小部件, 1

search_title, 搜索标题, 0
search_detail, 搜索全文, 0
sync_data, 同步云端数据 , 1
click_about_me, 关于开发者, 1
click_about_app, 关于app, 1
 */

object EventUtil {


    const val clickDonation = "click_donation"
    const val clickOpenInBrowser = "open_in_browser"
    const val clickShareByPicture = "click_share_by_picture"
    const val clickCopyLink = "copy_link"
    const val chooseJob = "choose_job"
    const val cancelRead = "cancel_has_read"
    const val searchTitle = "search_title"
    const val searchDetail = "search_detail"
    const val setListItemCount = "set_list_item_count" // 设置列表条目
    const val hideReadContent = "hide_read_content"// 隐藏已读文档
    const val clickLikeList = "click_like_list"
    const val clickHistoryList = "click_history_list"
    const val clickReadSetting = "click_read_setting"
    const val clickDownloadSetting = "click_download_setting"
    const val clickAboutApp = "click_about_app"
    const val clickAboutMe = "click_about_me"
    const val clickCopyright = "click_copyright"
    const val clickGameList = "click_game_list"// 点击游戏列表

    // v0.1.1添加
    const val clickReadDetail = "read_detail"
    const val clickRandomAll = "random_all"
    const val clickRandomScp = "random_scp"
    const val clickRandomTale = "random_tale"
    const val clickRandomJoke = "random_joke"
    const val clickDirect = "direct"
    const val clickLaterList = "later_read_list"
    const val addLater = "add_later_read"
    const val importReadList = "import_read_list"
    const val clickDraft = "draft"
    const val addWidget = "add_widget"

    fun onEvent(context: Context? = ScpApplication.context, event: String) {
        MobclickAgent.onEvent(context, event)
        TCAgent.onEvent(context, event)
    }
    fun onEvent(context: Context? = ScpApplication.context, event: String, arg: String) {
        MobclickAgent.onEvent(context, event, arg)
        TCAgent.onEvent(context, event)
    }
}