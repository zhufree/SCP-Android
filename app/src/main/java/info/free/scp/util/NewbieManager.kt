package info.free.scp.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import info.free.scp.view.detail.DetailActivity
import info.free.scp.view.home.PrivacyActivity

/**
 * 新人引导流程。
 * Created by zhufree on 2019/5/5.
 */
object NewbieManager {

    fun showPrivacyDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setMessage("使用本 APP 之前，请阅读隐私协议。")
            .setPositiveButton("确定") { dialog, _ ->
                PreferenceUtil.setCheckPrivacy()
                dialog.dismiss()
            }
            .setNeutralButton("查看隐私协议") { _, _ ->
                PreferenceUtil.setCheckPrivacy()
                activity.startActivity(Intent(activity, PrivacyActivity::class.java))
            }
            .show()
    }

    fun showLevelDialog(activity: Activity) {
        val levels = arrayOf(
            "对 SCP 了解不多或只知道衍生游戏",
            "资深读者 / 作者（路过简介）"
        )
        AlertDialog.Builder(activity)
            .setTitle("请选择你对 SCP 基金会的了解程度")
            .setItems(levels) { _, index ->
                PreferenceUtil.setFirstInstallApp()
                when (index) {
                    0 -> showNewReaderNotice(activity)
                    1 -> showExperiencedReaderNotice(activity)
                }
            }
            .show()
    }

    private fun showNewReaderNotice(activity: Activity) {
        val message = """
            SCP 是真的吗？

            不是。SCP 基金会是一个创意写作网站，所有 SCP 条目和基金会设定都是虚构作品。

            这些文章是谁写的？

            它们来自 SCP 基金会网站的创作者群体。这个 APP 主要是一个阅读器，内容来自 SCP 基金会中文维基，并加入了一些辅助阅读功能。

            SCP 故事需要按编号顺序阅读吗？

            不需要。编号没有严格的前后关系，看到感兴趣的文章就可以点进去阅读。

            如果你想从最初的作品开始，可以先阅读 SCP-173。
        """.trimIndent()

        AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton("阅读 SCP-173") { _, _ ->
                openDetail(activity, "/scp-173", "SCP-173")
            }
            .setNegativeButton("稍后再说", null)
            .show()
    }

    private fun showExperiencedReaderNotice(activity: Activity) {
        val message = """
            本 APP 的内容来自 SCP 基金会官网，目标是改善网络不稳定时的阅读体验，并提供一些辅助功能。

            如果是第一次尝试写作 SCP，建议先阅读《如何撰写一篇 SCP 文档》和指导中心。写作过程中遇到疑问，也可以在指导中心找到相应教程。

            如果对 APP 有改进建议或疑难反馈，欢迎加入 QQ 群 805194504 联系我们。
        """.trimIndent()

        AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton("查看指导中心") { _, _ ->
                openDetail(activity, "/guide-hub", "指导中心")
            }
            .setNegativeButton("稍后再说", null)
            .show()
    }

    private fun openDetail(activity: Activity, link: String, title: String) {
        activity.startActivity(
            Intent(activity, DetailActivity::class.java)
                .putExtra("link", link)
                .putExtra("title", title)
        )
    }
}
