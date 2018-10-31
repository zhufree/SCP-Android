package info.free.scp.util

import android.content.Context
import android.content.SharedPreferences
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