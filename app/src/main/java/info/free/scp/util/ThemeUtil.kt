package info.free.scp.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.LocalBroadcastManager
import info.free.scp.R
import info.free.scp.SCPConstants.ACTION_CHANGE_THEME
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/10/22.
 * 主题切换工具方法
 */

object ThemeUtil {
    var toolbarBg: Int = -1
    var containerBg: Int = -1
    var itemBg: Int = -1
    var darkText: Int = -1
    var lightText: Int = -1

    var currentTheme: Int = -1

    init {
        currentTheme = PreferenceUtil.getCurrentTheme()
    }

    /** 为每一个activity设置主题  */
    fun setTheme(activity: Activity) {
        when (currentTheme) {
            0 -> activity.setTheme(R.style.DayTheme)
            1 -> activity.setTheme(R.style.NightTheme)
            else -> activity.setTheme(R.style.DayTheme)
        }
    }

    fun setTheme(application: Application) {
        when (currentTheme) {
            0 -> application.setTheme(R.style.DayTheme)
            1 -> application.setTheme(R.style.NightTheme)
            else -> application.setTheme(R.style.DayTheme)
        }
    }


    /** 修改主题  */
    fun changeTheme(activity: Activity, theme: Int) {
        currentTheme = theme
        PreferenceUtil.setCurrentTheme(theme)
        setTheme(activity)
        initColor(activity)
        LocalBroadcastManager.getInstance(ScpApplication.context!!).sendBroadcast(Intent(ACTION_CHANGE_THEME))
    }

    private fun initColor(context: Context) {
        val allColor = context.obtainStyledAttributes(intArrayOf(
                R.attr.toolbarBg,
                R.attr.containerBg,
                R.attr.itemBg,
                R.attr.darkText,
                R.attr.lightText
        ))
        toolbarBg = allColor.getColor(0, Color.WHITE)
        containerBg = allColor.getColor(1, Color.WHITE)
        itemBg = allColor.getColor(2, Color.WHITE)
        darkText = allColor.getColor(3, Color.WHITE)
        lightText = allColor.getColor(4, Color.WHITE)
        allColor.recycle()
    }
}
