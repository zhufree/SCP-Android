package info.free.scp.util

import android.content.Context
import android.content.SharedPreferences
import info.free.scp.R
import info.free.scp.ScpApplication
import java.util.*
import java.util.Calendar.YEAR

/**
 * Created by zhufree on 2018/8/30.
 * SharedPreference工具类
 */

object PreferenceUtil {
    private fun getPrivateSharedPreference(name: String): SharedPreferences? {
        return ScpApplication.context?.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    fun getInitCategoryFinish(): Boolean {
        return getBooleanValue("init", "initData")
    }

    fun setInitCategoryFinish(finish: Boolean) {
        setBooleanValue("init", "initData", finish)
    }
    fun getDetailDataLoadCount(): Int {
        return getIntValue("init", "detailData")
    }

    fun addDetailDataLoadCount() {
        setIntValue("init", "detailData", getDetailDataLoadCount() +1)
    }

    fun resetDetailDataLoadCount() {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putInt("detailData", 0)?.apply()
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

    fun getLocalDbVersion(): Int {
        val sp = getPrivateSharedPreference("init")
        return sp?.getInt("dbVersion", 1)?:1
    }

    fun setLocalDbVersion(version: Int) {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putInt("dbVersion", version)?.apply()
        setLastUpdateDbTime()
    }

    /**
     * 存储当前正在更新的数据库版本
     */
    fun setTempUpdateDbVersion(version: Int) {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putInt("tempUpdateDb", version)?.apply()
    }

    fun getTempUpdateDbVersion(): Int {
        val sp = getPrivateSharedPreference("init")
        return sp?.getInt("tempUpdateDb", 0)?:0
    }

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

    /**
     * 数据更新时间相关
     */
    fun setLastUpdateDbTime() {
        setLongValue("update", "lastUpdateDbTime", System.currentTimeMillis())
    }

    fun getLastUpdateDbTime(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getLongValue("update", "lastUpdateDbTime")
        return if (calendar.get(YEAR) == 1970) "无" else calendar.time.toString()
    }

    fun setServerLastUpdateTime(time: String) {
        setStringValue("update", "serverLastUpdateTime", time)
    }

    fun getServerLastUpdateTime(): String {
        return getStringValue("update", "serverLastUpdateTime")
    }

    fun getQueryLink():String{
        return getStringValue("update", "queryLink")
    }

    fun saveQueryLink(link: String) {
        setStringValue("update", "queryLink", link)
    }

    /**
     * 积分系统
     */
    fun addPoints(point: Int) {
        setIntValue("level", "point", getPoint() + point)
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
                outer[level]
            }
        }
    }

    private val jobDesc = arrayOf(R.string.job_desc_1,R.string.job_desc_2,R.string.job_desc_3,
            R.string.job_desc_4,R.string.job_desc_5,R.string.job_desc_6)

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


    /**
     * 工具方法
     */
    private fun getBooleanValue(spName: String, key: String): Boolean {
        val sp = getPrivateSharedPreference(spName)
        return sp?.getBoolean(key, false)?:false
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