package info.free.scp.util

import info.free.scp.ScpApplication
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
        val levels = listOf("第一次听说scp基金会", "看过基金会的相关视频和游戏，但是不知道文档和中分主站的存在",
                "看过文档，也许还有主站账号", "事写手！")
        var noticeTitle = ""
        var noticeMessage = ""
        activity.selector("请选择你对SCP基金会的了解程度", levels) { _, i ->
            when (i) {
                0 -> {
                    noticeTitle = "第一次听说scp基金会"
                    noticeMessage = "SCP基金会是一个接力小说创作网站（SCP基金会维基）所创作出的虚拟组织，" +
                            "网友们在此创作基于同一世界观下的脑洞和设定写成文档，甚至由文档中的物体衍生出很多同人作品（视频，游戏等）。" +
                            "SCP基金会维基是一个全球性的组织，所以有些文档是翻译自国外的SCP主站（SCP系列），" +
                            "有些则是中国分部主站原创（SCP-CN系列）" +
                            "以及其他很多有趣的内容，欢迎你来探索。点击下载离线文档数据库，下载完成后即可离线阅读文档（不包括图片）。"
                }
                1 -> {
                    noticeTitle = "文档才是本体（虽然它们看起来像说明书"
                    noticeMessage = "欢迎你，云爱好者，从这里你可以开始接触SCP基金会的真正面貌，了解更多不广为人知但同样吸引人的SCP们。" +
                            "点击下载离线文档数据库，下载完成后即可离线阅读文档（不包括图片）。"
                }
                2 -> {
                    noticeTitle = "来啦老弟！"
                    noticeMessage = "本app是SCP基金会中分网站的移动端阅读工具，所有内容均来自网站，app更新时间不固定，有任何意见反馈欢迎加群催更。" +
                            "点击下载离线文档数据库，完成后即可离线阅读文档（不包括图片）。"
                }
                3 -> {
                    noticeTitle = "感谢你的创作"
                    noticeMessage = "本app是SCP基金会中分网站的移动端阅读工具，所有内容均来自网站，希望能给大家更方便的阅读体验。" +
                            "app更新时间不固定，有任何意见反馈欢迎加群催更。" +
                            "点击下载离线文档数据库，完成后即可离线阅读文档（不包括图片）。"
                }
            }
            activity.alert {
                title = noticeTitle
                message = noticeMessage
                yesButton { }
//                if (!FileUtil.getInstance(ScpApplication.context).checkDataReady() // 没有数据库
//                        && PreferenceUtil.getDownloadLink().isNotEmpty()) { // 链接准备好了
//                    neutralPressed("点击下载文档数据库") {
//                        activity.startActivity<DownloadActivity>()
//                    }
//                }

            }.show()
        }
    }
}