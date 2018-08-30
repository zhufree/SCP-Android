package info.free.scp.util

import android.content.Context
import android.content.SharedPreferences
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/8/30.
 *
 */

object PreferenceUtil {
    private fun getPrivateSharedPreference(name: String): SharedPreferences? {
        return ScpApplication.context?.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    fun getInit(): Boolean {
        val sp = getPrivateSharedPreference("init")
        return sp?.getBoolean("init", false)?:false
    }
    fun setInit() {
        val sp = getPrivateSharedPreference("init")
        sp?.edit()?.putBoolean("init", true)?.apply()
    }
}