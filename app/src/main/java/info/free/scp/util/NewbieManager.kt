package info.free.scp.util

import info.free.scp.view.base.BaseActivity
import info.free.scp.view.download.DownloadActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton

/**
 * 新人引导流程
 * Created by zhufree on 2019/5/5.
 */

object NewbieManager {

    fun showLevelDialog(activity: BaseActivity) {
        val levels = listOf("第一次听说scp基金会", "看过基金会的相关视频和游戏，但是不知道文档和主站","看过文档，也许还有主站账号", "事写手！")
        var noticeTitle = ""
        var noticeMessage = ""
        activity.selector("请选择你对SCP基金会的了解程度", levels) { _, i ->
            when (i) {
                0 -> {
                    noticeTitle = "第一次听说scp基金会"
                    noticeMessage = "SCP基金会是一个接力小说网站，网友们在此创作基于同一世界观下的脑洞和设定..."
                }
                1 -> {
                    noticeTitle = "文档才是本体（虽然它们看起来像说明书"
                    noticeMessage = "欢迎你，云爱好者，从这里你可以开始接触SCP基金会的真正面貌..."
                }
                2 -> {
                    noticeTitle = "来啦老弟！"
                    noticeMessage = "本app是SCP基金会中分网站的移动端阅读工具，所有内容均来自网站，更新时间不固定，点击确定后开始获取网站文章目录，" +
                            "之后请点击以下链接下载离线文档数据库，下载完成后即可离线阅读文档（不包括图片）"
                }
                3 -> {
                    noticeTitle = "感谢你的创作"
                    noticeMessage = "本app是SCP基金会中分网站的移动端阅读工具，所有内容均来自网站，更新时间不固定，点击确定后开始获取网站文章目录，" +
                            "之后请点击以下链接下载离线文档数据库，下载完成后即可离线阅读文档（不包括图片）"
                }
            }
            activity.alert {
                title = noticeTitle
                message = noticeMessage
                yesButton {  }
                neutralPressed("点击下载文档数据库") {
                    // TODO 跳转
                    activity.startActivity<DownloadActivity>()
                }

            }.show()
        }
    }
}