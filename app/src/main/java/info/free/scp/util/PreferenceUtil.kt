package info.free.scp.util

import android.content.Context
import android.content.SharedPreferences
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/8/30.
 * SharedPreference工具类
 */

object PreferenceUtil {
    private fun getPrivateSharedPreference(name: String): SharedPreferences? {
        return ScpApplication.context?.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    fun getInitAboutData(): Boolean {
        return getBooleanValue("init", "initAbout")
    }
    fun setInitAboutData() {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putBoolean("initAbout", true)?.apply()
    }

    fun getInitDataFinish(): Boolean {
        return getBooleanValue("init", "initData")
    }

    fun setInitDataFinish(finish: Boolean) {
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
        sp?.edit()?.putBoolean(versionCode, true)?.apply()
    }

    fun getLocalDbVersion(): Int {
        val sp = getPrivateSharedPreference("init")
        return sp?.getInt("dbVersion", 1)?:1
    }

    fun setLocalDbVersion(version: Int) {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putInt("dbVersion", version)?.apply()
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
}