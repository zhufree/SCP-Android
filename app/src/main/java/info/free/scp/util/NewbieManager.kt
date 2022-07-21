package info.free.scp.util

import info.free.scp.view.base.BaseActivity
import info.free.scp.view.detail.DetailActivity
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
        val levels = listOf("·对SCP了解不多或只知道衍生游戏", "·资深读者/作者（跳过简介）")
        var noticeTitle = ""
        var noticeMessage = ""
        activity.selector("请选择你对SCP基金会的了解程度", levels) { _, i ->
            when (i) {
                0 -> {
                    noticeMessage = "SCP是真的吗？\n\n" +
                            "不。那是一个创意写作网站。所有的SCP都是虚构的。基金会是虚构的。\n\n" +
                            "所有的这些文章是你们还是其他什么人写的？\n\n" +
                            "是网站的创作者们写的，一个作家团体。为了弥补网站的网络不稳定，同时添加辅助功能，我们开发了这款APP，内容来自于SCP基金会官网（http://scp-wiki-cn.wikidot.com/）。可以这说是一个小说阅读器。\n" +
                            "\n" +
                            "SCP的故事现在还在连载中吗？\n\n" +
                            "“SCP基金会”只是一个主题，不是一部连载小说。成千上万名作者在这一主题下创作自己的独立故事，这些故事的剧情并不互相关联，就像学校里的命题作文。\n" +
                            "\n" +
                            "在一些情况下，一些作者会在读完别人的作品后选择将它续写下去，此时新作品会在明显位置提示你先阅读原作品，而被续写的原作很可能不会有任何能表现续写出现的改动（原作者可能都联系不上了）。\n" +
                            "\n" +
                            "是的，你可能会因此见到两个作者续写的不同结局。是的，你甚至能见到改写原先故事走向的“if线”。是的，如果你的作品足够有趣，也可能会被其他人续写。\n" +
                            "\n" +
                            "基金会主题下所有数千篇独立作品就源自于你刚才所看到的那篇“SCP-173”。它被称为“最初之作”。\n" +
                            "\n" +
                            "那我能不能找到一些这个主题的共同前提设定？\n\n" +
                            "可以看看[http://scp-wiki-cn.wikidot.com/about-the-scp-foundation 这里]。也有不少人建立了[http://scp-wiki-cn.wikidot.com/canon-hub 设定中心]来描绘“假如如此”的故事。\n" +
                            "\n" +
                            "我应该按编号顺序阅读SCP吗？\n\n" +
                            "不必。在写作时，编号是随意确定的，没有前后关系。看到你感兴趣的文章就点进去看看吧？\n\n" +
                            "这里有SCP相关游戏吗？\n\n" +
                            "本APP主要面向读者群体，不过我们尽力汇总了一些游戏在[我的-游戏列表]这里。"

                    activity.alert {
                        message = noticeMessage
                        yesButton {
                            activity.startActivity<DetailActivity>("link" to "/scp-173",
                                "title" to "SCP-173")
                        }
                    }.show()

                }
                1 -> {
                    noticeMessage = "为了弥补网站的网络不稳定，同时添加辅助功能，我们开发了这款APP，内容来自于SCP基金会官网（http://scp-wiki-cn.wikidot.com/）。可以这说是一个小说阅读器。\n" +
                            "\n" +
                            "如果是首次尝试写作SCP，记得阅读[http://scp-wiki-cn.wikidot.com/how-to-write-an-scp 《如何撰写一篇SCP文档》]。写作过程中遇到的各种疑难杂症也可以在[http://scp-wiki-cn.wikidot.com/guide-hub 指导中心]找到相应的教程。\n" +
                            "\n" +
                            "如果对APP有任何改进建议或疑难，欢迎加入[805194504 QQ群]联系我们"
                    activity.alert {
                        title = noticeTitle
                        message = noticeMessage
                        yesButton {
                            activity.startActivity<DetailActivity>("link" to "/guide-hub",
                                "title" to "指导中心")
                        }
                    }.show()
                }
            }

        }
    }
}