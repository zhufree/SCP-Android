package info.free.scp.util

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import info.free.scp.BuildConfig
import info.free.scp.R
import info.free.scp.SCPConstants
import info.free.scp.service.HttpManager
import info.free.scp.view.base.BaseActivity
import kotlinx.android.synthetic.main.layout_dialog_report.view.*
import org.jetbrains.anko.doAsync


class UpdateManager(private var activity: BaseActivity) {

    init {
        manager = this
    }

    companion object {
        var manager: UpdateManager? = null

        fun getInstance(newActivity: BaseActivity): UpdateManager {
            manager?.activity = newActivity
            return manager ?: UpdateManager(newActivity)
        }
    }

    private val currentVersionCode = BuildConfig.VERSION_CODE

    private var mLocalBroadcastManager: androidx.localbroadcastmanager.content.LocalBroadcastManager? = null

    /**
     * 检测更新和数据初始化
     * ---start---
     * 0. 第一次启动，进入新手引导流程，不检查更新
     * 1. 检测app版本，确保是最新版：checkUpdateAndCategory()
     * 2. 检测本地是否有备份数据，如果有，执行恢复并把初始化标记位都置为true，跳转5，否则转3
     * 3. 检测目录是否初始化，加载目录，完毕后加载正文
     * 4. 检测正文是否加载完成，没有的话继续加载
     * 5. 录已加载完的前提下，检测姓名和职位是否存在（一般是第二天打开了，提示输入姓名和职位
     * ---finish---
     */
    fun checkAppData() {

        Logger.i("start checkAppData()")
        if (PreferenceUtil.isFirstInstallApp()) {
            // 显示新人引导，下次再检测更新
            PreferenceUtil.setFirstInstallApp()
            NewbieManager.showLevelDialog(activity)
            FileHelper.getInstance(activity).restoreDB() // 第一次安装app时检测

//            return
        }
        if (PreferenceUtil.getFirstOpenCurrentVersion(currentVersionCode.toString())) {
            // 当前版本第一次启动app，把检测更新时间重置，再检测一次更新
            PreferenceUtil.setFirstOpenCurrentVersion(currentVersionCode.toString())

            // FIXME 打包后下版本开发删掉这块代码
            // TODO 第一次安装这个新版本检测：如果本地有数据库，同步like等信息 第一次安装app和第一次安装这个版本区分一下
            // TODO 把原数据库改名成info，然后删掉data类的信息
        }

        // 普通情况下一天检测一次更新
        if (Utils.enabledNetwork(activity)) {
            PreferenceUtil.addPoints(1)
            Log.i("scp", "checkUpdate")
            // 记录上次检测更新时间
            // 检测app版本，如果有更新版本就不加载目录，等更新之后再加载
            doAsync {
                // 检查更新信息
                var newVersionCode = 0
                var updateDesc: String? = ""
                var updateLink: String? = ""
                HttpManager.instance.getAppConfig {
                    Logger.i("getAppConfig result = $it")
                    for (config in it) {
                        if (config.key == "version") {
                            newVersionCode = config.value.toInt()
                        }
                        if (config.key == "update_desc") {
                            updateDesc = config.value
                        }
                        if (config.key == "update_link") {
                            updateLink = config.value
                        }
                        // 上次数据更新时间，分几个库
                        if (config.key.contains("last_update_time")) {
                            PreferenceUtil.setServerLastUpdateTime(config.key, config.value)
                        }
                        // 数据下载链接
                        if (config.key.contains("db_link")) {
                            PreferenceUtil.setDataDownloadLink(config.key, config.value)
                        }
                    }
                    if (currentVersionCode < newVersionCode && !activity.isFinishing) {
                        Logger.i("current = $currentVersionCode, new = $newVersionCode, 需要升级")
                        AlertDialog.Builder(activity)
                                .setTitle("发现新版本")
                                .setMessage(updateDesc)
                                .setPositiveButton("现在升级") { _, _ ->
                                    val updateIntent = Intent()
                                    updateIntent.action = "android.intent.action.VIEW"
                                    val updateUrl = Uri.parse(updateLink)
                                    updateIntent.data = updateUrl
                                    activity.startActivity(updateIntent)
                                }
                                .setNegativeButton("暂不升级") { _, _ -> }
                                .create().show()
                        // 有新版本就不检查数据更新，知道更新到最新
                    }
                }

            }
        }
    }


    fun checkUserInfo() {
        if (PreferenceUtil.getInitCategoryFinish() && PreferenceUtil.getNickname().isEmpty()
                && !activity.isFinishing) {
            val inputView = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_report, null)
            val nameInputDialog = AlertDialog.Builder(activity)
                    .setTitle("欢迎来到SCP基金会，调查员，请输入你的名字（重启app后生效显示）")
                    .setView(inputView)
                    .setPositiveButton("OK") { _, _ -> }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .create()
            nameInputDialog.show()
            nameInputDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                PreferenceUtil.saveNickname(inputView.et_report.text.toString())
                nameInputDialog.dismiss()
                checkJob()
            }
        }
        checkJob()
    }

    private fun checkJob() {
        if (PreferenceUtil.getNickname().isNotEmpty() && PreferenceUtil.getJob().isEmpty()
                && !activity.isFinishing) {
            val jobList = arrayOf("收容专家", "研究员", "安全人员", "战术反应人员", "外勤特工", "机动特遣队作业员")
            AlertDialog.Builder(activity)
                    .setTitle("欢迎来到SCP基金会，${PreferenceUtil.getNickname()}，请选择你的职业")
                    .setItems(jobList) { out, which ->
                        val field = out?.javaClass?.superclass?.getDeclaredField(
                                "mShowing")
                        field?.isAccessible = true
                        //   将mShowing变量设为false，表示对话框已关闭
                        field?.set(out, false)
                        AlertDialog.Builder(activity)
                                .setTitle(jobList[which])
                                .setMessage(PreferenceUtil.getDescForJob(jobList[which]))
                                .setPositiveButton("确定选择（暂时不可更改）") { inner, _ ->
                                    PreferenceUtil.setJob(jobList[which])
                                    EventUtil.onEvent(activity, EventUtil.chooseJob, jobList[which])
                                    FileHelper.getInstance(activity).backUp()
                                    inner.dismiss()
                                    field?.set(out, true)
                                    out.dismiss()
                                }
                                .setNegativeButton("我手滑了") { dialog, _ -> dialog.dismiss() }
                                .create().show()
                    }
                    .create().show()
        }
    }
}