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
        val sp = getPrivateSharedPreference("init")
        return sp?.getBoolean("initAbout", false)?:false
    }
    fun setInitAboutData() {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putBoolean("initAbout", true)?.apply()
    }

    fun getInitDataFinish(): Boolean {
        val sp = getPrivateSharedPreference("init")
        return sp?.getBoolean("initData", false)?:false
    }

    fun setInitDataFinish() {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putBoolean("initData", true)?.apply()
    }
}