package info.free.scp.util

import android.content.Context
import com.umeng.analytics.MobclickAgent
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/9/21.
 *
 * cancel_has_read, 取消已读的文档, 1
choose_job, 选择基金会职业, 0
copy_link, 复制文档链接 , 0
open_in_broswer, 在浏览器打开文档, 0
like, 收藏文档, 0
click_donation, 点击捐赠开发者, 1
random, 随机文档, 1
search_title, 搜索标题, 0
search_detail, 搜索全文, 0
sync_data, 同步云端数据 , 1
click_about_me, 关于开发者, 1
click_about_app, 关于app, 1
finish_read, 标记已读, 1
 */

object EventUtil {

    const val clickAboutApp = "click_about_app"
    const val clickAboutMe = "click_about_me"
    const val clickSyncData = "sync_data"
    const val clickRandom = "random"
    const val clickDonation = "click_donation"
    const val clickLike = "like"
    const val clickOpenInBrowser = "open_in_broswer"
    const val clickCopyLink = "copy_link"
    const val chooseJob = "choose_job"
    const val cancelRead = "cancel_has_read"
    const val finishRead = "finish_read"
    const val searchTitle = "search_title"
    const val searchDetail = "search_detail"

    fun onEvent(context: Context? = ScpApplication.context, event: String) {
        MobclickAgent.onEvent(context, event)
    }
    fun onEvent(context: Context? = ScpApplication.context, event: String, arg: String) {
        MobclickAgent.onEvent(context, event, arg)
    }
}