package info.free.scp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import info.free.scp.R
import info.free.scp.ScpApplication
import java.util.*

/**
 * Created by zhufree on 2018/8/30.
 * SharedPreference工具类
 */

object PreferenceUtil {

    const val INIT_SP = "init"
    const val DOWNLOAD_SP = "download"
    const val APP_SP = "app"

    private fun getPrivateSharedPreference(name: String): SharedPreferences? {
        return ScpApplication.context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    /**
     * -------------------数据初始化----------------------
     */

    /**
     * 检测是不是第一次安装app
     */
    fun isFirstInstallApp(): Boolean {
        return getBooleanValue(INIT_SP, "firstInstall", true)
    }

    fun setFirstInstallApp() {
        setBooleanValue(INIT_SP, "firstInstall", false)
    }


    /**
     * 服务器数据更新时间，last_update_time_0/1/2/3/4
     * key是字段名
     */
    fun setServerLastUpdateTime(time: Long) {
        setLongValue(DOWNLOAD_SP, "last_update_time", time)
    }

    fun getServerLastUpdateTime(): Long {
        return getLongValue(DOWNLOAD_SP, "last_update_time")
    }

    fun setNotice(notice: String) {
        setStringValue(APP_SP, "notice", notice)
    }

    fun getNotice(): String {
        return getStringValue(APP_SP, "notice")
    }

    /**
     * 分库数据文件下载链接
     * db_link_0/1/2/3/4/all
     */
    fun setDataDownloadLink(dbName: String, link: String) {
        setStringValue(DOWNLOAD_SP, dbName, link)
    }

    fun getDataDownloadLink(dbIndex: Int): String {
        return getStringValue(DOWNLOAD_SP, "db_link_" + if (dbIndex == -1) "all" else dbIndex)
    }

    // 新版本，只下一个数据库
    fun setDownloadLink(link: String) {
        setStringValue(DOWNLOAD_SP, "db_link", link)
    }

    fun getDownloadLink(): String {
        return getStringValue(DOWNLOAD_SP, "db_link")
    }

    /**
     * 记录单个库离线完成，同时记录离线时间，在离线管理页用到
     */


//    fun getDetailDataLoadFinish(dbIndex: Int): Boolean {
//        return getBooleanValue(INIT_SP, dbIndex.toString(), false)
//    }


    /**
     * 数据更新时间相关
     */
    fun getDetailLastLoadTime(): Long {
        return getLongValue(DOWNLOAD_SP, "load_db_time")
    }

    fun setDetailLastLoadTime(time: Long) {
        setLongValue(DOWNLOAD_SP, "load_db_time", time)
    }

    fun clearDownloadPref() {
        getPrivateSharedPreference(DOWNLOAD_SP)?.edit()?.clear()?.apply()
    }


    /**
     * 检测当前版本是不是第一次启动
     */
    fun getFirstOpenCurrentVersion(versionCode: String): Boolean {
        val sp = getPrivateSharedPreference(INIT_SP)
        return sp?.getBoolean(versionCode, true) ?: true
    }

    fun setFirstOpenCurrentVersion(versionCode: String) {
        val sp = getPrivateSharedPreference(INIT_SP)
        sp?.edit()?.putBoolean(versionCode, false)?.apply()
    }

    /**
     * 第一次进正文是否显示过提示
     */
    fun getShownDetailNotice(): Boolean {
        val sp = getPrivateSharedPreference(INIT_SP)
        return sp?.getBoolean("shownDetailNotice", false) ?: false
    }

    fun setShownDetailNotice() {
        val sp = getPrivateSharedPreference(INIT_SP)
        sp?.edit()?.putBoolean("shownDetailNotice", true)?.apply()
    }

    /**
     * 主题相关
     */
    fun getCurrentTheme(): Int {
        return getIntValue("theme", "currentTheme")
    }

    fun setCurrentTheme(theme: Int) {
        setIntValue("theme", "currentTheme", theme)
    }

    /**
     * 检测小组件当天是否更新
     */

    fun setLastUpdateWidgetTime(time: Long) {
        setLongValue(APP_SP, "lastUpdateWidgetTime", time)
    }

    fun checkNeedUpdateWidget(): Boolean {
        val lastTime = getLongValue(APP_SP, "lastUpdateWidgetTime")
        val cal = Calendar.getInstance()
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0)
        val beginOfDate = cal.timeInMillis
        return lastTime < beginOfDate
    }


    /**
     * 积分系统
     */
    fun addPoints(point: Int) {
        setIntValue("level", "point", getPoint() + point)
    }

    fun reducePoints(point: Int) {
        setIntValue("level", "point", getPoint() - point)
    }

    fun getPoint(): Int {
        return getIntValue("level", "point")
    }

    fun getNickname(): String {
        return getStringValue("level", "nickname")
    }

    fun saveNickname(name: String) {
        setStringValue("level", "nickname", name)
    }

    fun getJob(): String {
        return getStringValue("level", "job")
    }

    fun setJob(job: String) {
        setStringValue("level", "job", job)
    }

    /**
     * 职位头衔升级
     * 外勤特工
     * 机动特遣队作业员
     * 收容专家
     * 研究员
     * 安全人员
     * 战术反应人员
     */
    private val outer = arrayOf("实习外勤特工", "支援外勤特工", "辅助外勤特工", "外勤特工", "独立外勤特工", "特级外勤特工")
    private val specialWork = arrayOf("实习机动特遣队作业员", "支援机动特遣队作业员", "机动特遣队参谋",
            "机动特遣队队长", "机动特遣队指挥官", "机动特遣队主管")
    private val specialist = arrayOf("助理收容技师", "收容技师", "助理收容工程师", "收容工程师", "高级收容工程师", "收容研究员")
    private val researcher = arrayOf("见习研究员", "助理研究员", "副研究员", "研究员", "高级研究员", "研究员主管")
    private val safer = arrayOf("实习守卫", "一级守卫", "二级守卫", "守卫组长", "守卫参谋", "保安官")
    private val tactical = arrayOf("实习战术反应人员", "支援战术反应人员", "一级战术反应人员", "二级战术反应人员",
            "三级战术反应人员", " 战术小组组长")

    fun getJobNameByLevel(level: Int): String {
        val job = getJob()
        return when (job) {
            "外勤特工" -> {
                outer[level]
            }
            "机动特遣队作业员" -> {
                specialWork[level]
            }
            "收容专家" -> {
                specialist[level]
            }
            "研究员" -> {
                researcher[level]
            }
            "安全人员" -> {
                safer[level]

            }
            "战术反应人员" -> {
                tactical[level]
            }
            else -> {
                ""
            }
        }
    }

    private val jobDesc = arrayOf(R.string.job_desc_1,
            R.string.job_desc_2,
            R.string.job_desc_3,
            R.string.job_desc_4,
            R.string.job_desc_5,
            R.string.job_desc_6)

    fun getDescForJob(job: String): Int {
        return when (job) {
            "收容专家" -> {
                jobDesc[0]
            }
            "研究员" -> {
                jobDesc[1]
            }
            "安全人员" -> {
                jobDesc[2]
            }
            "战术反应人员" -> {
                jobDesc[3]
            }
            "外勤特工" -> {
                jobDesc[4]
            }
            "机动特遣队作业员" -> {
                jobDesc[5]
            }
            else -> {
                jobDesc[6]
            }
        }
    }

    fun getHideFinished(): Boolean {
        return getBooleanValue("read_settings", "hide_finished_article")
    }

    fun getCategoryCount(): Int {
        val count = getStringValue("read_settings", "category_count")
        return if (count.isNotEmpty()) count.toInt() else 100
    }

    fun getCategoryHeight(): Int {
        val height = getStringValue("read_settings", "category_height")
        return if (height.isNotEmpty()) height.toInt() else 50
    }

    fun getCategoryInterval(): Int {
        val interval = getStringValue("read_settings", "category_interval")
        return if (interval.isNotEmpty()) interval.toInt() else 4
    }

    fun getDetailTextSize(): String {
        val textSize = getStringValue("read_settings", "detail_text_size")
        return if (textSize.isNotEmpty()) textSize else "16px"
    }

    fun setDetailTextSize(size: String) {
        setStringValue("read_settings", "detail_text_size", size)
    }

    /**
     * 草稿临时储存
     * 数据库换架构时再加多个草稿列表
     */
    fun saveDraftContent(content: String) {
        setStringValue("level", "draft_content", content)
    }

    fun getDraftContent(): String {
        return getStringValue("level", "draft_content")
    }

    fun saveDraftTitle(title: String) {
        setStringValue("level", "draft_title", title)
    }

    fun getDraftTitle(): String {
        return getStringValue("level", "draft_title")
    }

    /**
     * 广告
     */
    fun setShowAdNotice() {
        setBooleanValue(APP_SP, "show_ad_notice", true)
    }

    fun getShowAdNotice() = getBooleanValue(APP_SP, "show_ad_notice")

    fun addAdPoints(point: Int) {
        setIntValue("level", "ad_point", getAdPoint() + point)
    }

    fun getAdPoint(): Int {
        return getIntValue("level", "ad_point")
    }

    fun setLastShowAdTime(time: Long) {
        setLongValue(APP_SP, "last_show_ad_time", time)
    }

    fun getLastShowAdTime() = getLongValue(APP_SP, "last_show_ad_time")

    /**
     * 工具方法
     */
    private fun getBooleanValue(spName: String, key: String, defaultValue: Boolean = false): Boolean {
        return getPrivateSharedPreference(spName)?.getBoolean(key, defaultValue) ?: defaultValue
    }

    private fun setBooleanValue(spName: String, key: String, value: Boolean) {
        getPrivateSharedPreference(spName)?.edit {
            putBoolean(key, value)
        }
    }

    private fun getIntValue(spName: String, key: String): Int {
        return getPrivateSharedPreference(spName)?.getInt(key, 0) ?: 0
    }

    private fun setIntValue(spName: String, key: String, value: Int) {
        getPrivateSharedPreference(spName)?.edit {
            putInt(key, value)
        }
    }

    private fun getLongValue(spName: String, key: String): Long {
        return getPrivateSharedPreference(spName)?.getLong(key, 0L) ?: 0L
    }

    private fun setLongValue(spName: String, key: String, value: Long) {
        getPrivateSharedPreference(spName)?.edit {
            putLong(key, value)
        }
    }

    private fun getStringValue(spName: String, key: String): String {
        return getPrivateSharedPreference(spName)?.getString(key, "") ?: ""
    }

    private fun setStringValue(spName: String, key: String, value: String) {
        getPrivateSharedPreference(spName)?.edit {
            putString(key, value)
        }
    }
}