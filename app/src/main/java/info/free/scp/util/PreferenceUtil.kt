package info.free.scp.util

import android.content.Context
import android.content.SharedPreferences
import info.free.scp.R
import info.free.scp.ScpApplication
import java.util.*

/**
 * Created by zhufree on 2018/8/30.
 * SharedPreference工具类
 */

object PreferenceUtil {
    private fun getPrivateSharedPreference(name: String): SharedPreferences? {
        return ScpApplication.context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    /**
     * -------------------数据初始化----------------------
     */
    /**
     * 目录和数据库是否离线完成，用boolean记录
     */
    fun getInitCategoryFinish(): Boolean {
        return getBooleanValue("init", "init_category")
    }

    fun setInitCategoryFinish(finish: Boolean) {
        setBooleanValue("init", "init_category", finish)
    }

    /**
     * 记录单个库离线完成，同时记录离线时间，在离线管理页用到
     */
    fun setDetailDataLoadFinish(downloadType: Int, value: Boolean) {
        if (value) {
            setDetailLastLoadTime(downloadType, Utils.formatNow())
        }
        setBooleanValue("init", downloadType.toString(), value)
    }
    fun getDetailDataLoadFinish(downloadType: Int): Boolean {
        return getBooleanValue("init", downloadType.toString(), true)
    }

    /**
     * 下载进度记录
     */
    fun getSingleDbLoadCount(downloadType: Int): Int {
        return getIntValue("download", downloadType.toString())
    }
    fun setSingleDbLoadCount(downloadType: Int, progress: Int) {
        setIntValue("download", downloadType.toString(), progress)
    }

    fun addSingleDbLoadCount(downloadType: Int) {
        setIntValue("download", downloadType.toString(), getSingleDbLoadCount(downloadType) +1)
    }
    fun resetSingleDbLoadCount(downloadType: Int) {
        setSingleDbLoadCount(downloadType, 0)
    }
    /**
     * 数据更新时间相关
     */
    fun getDetailLastLoadTime(downloadType: Int): String {
        return getStringValue("download","${downloadType}_time" )
    }
    fun setDetailLastLoadTime(downloadType: Int, time: String) {
        setStringValue("download", "${downloadType}_time", time)
    }


    /**
     * 检测当前版本是不是第一次启动
     */
    fun getFirstOpenCurrentVersion(versionCode: String): Boolean {
        val sp = getPrivateSharedPreference("init")
        return sp?.getBoolean(versionCode, true)?:true
    }

    fun setFirstOpenCurrentVersion(versionCode: String) {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putBoolean(versionCode, false)?.apply()
    }

    /**
     * 第一次进正文是否显示过提示
     */
    fun getShownDetailNotice(): Boolean {
        val sp = getPrivateSharedPreference("init")
        return sp?.getBoolean("shownDetailNotice", false)?:false
    }
    fun setShownDetailNotice() {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putBoolean("shownDetailNotice", true)?.apply()
    }

    /**
     * 主题相关
     */
    fun getCurrentTheme(): Int{
        return getIntValue("theme", "currentTheme")
    }

    fun setCurrentTheme(theme: Int) {
        setIntValue("theme", "currentTheme", theme)
    }


    /**
     * 检测更新，一天只检测一次，重新安装后要重置
     */
    fun setLastCheckUpdateTime(time: Long) {
        setLongValue("update", "lastCheckUpdateTime", time)
    }

    fun checkNeedShowUpdateNotice(): Boolean {
        val lastTime = getLongValue("update", "lastCheckUpdateTime")
        val cal = Calendar.getInstance()
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0)
        val beginOfDate = cal.timeInMillis
        return lastTime < beginOfDate
    }


    fun setServerLastUpdateTime(dbIndex: String, time: String) {
        setStringValue("update", dbIndex, time)
    }

    fun getServerLastUpdateTime(dbIndex: Int): String {
        return getStringValue("update", "last_update_time_$dbIndex")
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
            else ->{
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

    fun getDescForJob(job: String): Int{
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
            else ->{
                jobDesc[6]
            }
        }
    }

    fun getIfHideFinished(): Boolean {
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
     * 工具方法
     */
    private fun getBooleanValue(spName: String, key: String, defaultValue: Boolean = false): Boolean {
        val sp = getPrivateSharedPreference(spName)
        return sp?.getBoolean(key, defaultValue)?:defaultValue
    }
    private fun setBooleanValue(spName: String, key: String, value: Boolean) {
        val sp = getPrivateSharedPreference(spName)
        sp?.edit()?.putBoolean(key, value)?.apply()
    }
    private fun getIntValue(spName: String, key: String): Int {
        val sp = getPrivateSharedPreference(spName)
        return sp?.getInt(key, 0)?:0
    }

    private fun setIntValue(spName: String, key: String, value: Int) {
        val sp = getPrivateSharedPreference(spName)
        sp?.edit()?.putInt(key, value)?.apply()
    }

    private fun getLongValue(spName: String, key: String): Long {
        val sp = getPrivateSharedPreference(spName)
        return sp?.getLong(key, 0L)?:0L
    }

    private fun setLongValue(spName: String, key: String, value: Long) {
        val sp = getPrivateSharedPreference(spName)
        sp?.edit()?.putLong(key, value)?.apply()
    }
    private fun getStringValue(spName: String, key: String): String {
        val sp = getPrivateSharedPreference(spName)
        return sp?.getString(key, "")?:""
    }

    private fun setStringValue(spName: String, key: String, value: String) {
        val sp = getPrivateSharedPreference(spName)
        sp?.edit()?.putString(key, value)?.apply()
    }
}