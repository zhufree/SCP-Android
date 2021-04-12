package info.free.scp.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import info.free.scp.R
import info.free.scp.SCPConstants.BroadCastAction.ACTION_CHANGE_THEME
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/10/22.
 * 主题切换工具方法
 */

object ThemeUtil {
    var toolbarBg: Int = -1
    var containerBg: Int = -1
    var itemBg: Int = -1
    var accentColor: Int = -1
    var darkText: Int = -1
    var mediumText: Int = -1
    var lightText: Int = -1
    var disabledBg: Int = -1
    var linkBlue: Int = -1
    var todoYellow: Int = -1
    var dividerGray: Int = -1
    var currentTheme: Int = -1
    const val DAY_THEME = 0
    const val NIGHT_THEME = 1

    init {
        currentTheme = PreferenceUtil.getCurrentTheme()
    }

    /** 为每一个activity设置主题  */
    fun setTheme(activity: Activity) {
        when (currentTheme) {
            DAY_THEME -> activity.setTheme(R.style.DayTheme)
            NIGHT_THEME -> activity.setTheme(R.style.NightTheme)
            else -> activity.setTheme(R.style.DayTheme)
        }
    }

    fun setTheme(application: Application) {
        when (currentTheme) {
            DAY_THEME -> application.setTheme(R.style.DayTheme)
            NIGHT_THEME -> application.setTheme(R.style.NightTheme)
            else -> application.setTheme(R.style.DayTheme)
        }
        initColor(application)
    }


    /** 修改主题  */
    fun changeTheme(activity: FragmentActivity?, theme: Int) {
        currentTheme = theme
        PreferenceUtil.setCurrentTheme(theme)
        activity?.let {
            setTheme(activity)
            initColor(activity)
        }
        LocalBroadcastManager.getInstance(ScpApplication.context).sendBroadcast(Intent(ACTION_CHANGE_THEME))
    }

    private fun initColor(context: Context) {
        val allColor = context.obtainStyledAttributes(intArrayOf(
                R.attr.toolbarBg,
                R.attr.containerBg,
                R.attr.accentColor,
                R.attr.itemBoxBg,
                R.attr.darkText,
                R.attr.mediumText,
                R.attr.lightText,
                R.attr.disabledBg,
                R.attr.linkBlue,
                R.attr.todoYellow,
                R.attr.dividerGray
        ))
        toolbarBg = allColor.getColor(0, Color.WHITE)
        containerBg = allColor.getColor(1, Color.WHITE)
        accentColor = allColor.getColor(2, Color.WHITE)
        itemBg = allColor.getColor(3, Color.WHITE)
        darkText = allColor.getColor(4, Color.WHITE)
        mediumText = allColor.getColor(5, Color.WHITE)
        lightText = allColor.getColor(6, Color.WHITE)
        disabledBg = allColor.getColor(7, Color.WHITE)
        linkBlue = allColor.getColor(8, Color.WHITE)
        todoYellow = allColor.getColor(9, Color.WHITE)
        dividerGray = allColor.getColor(10, Color.WHITE)
        allColor.recycle()
    }

    /**
     *
     * @param fillColor
     * //填充色
     * @param strokeColor
     * //边框颜色
     * @param strokeWidth
     * //边框宽度
     * @param roundRadius
     * //圆角值
     * @return
     */
    fun customShape(fillColor: Int, strokeColor: Int, strokeWidth: Int, roundRadius: Int)
            : GradientDrawable {
        val gd = GradientDrawable() // 创建drawable
        gd.setColor(fillColor)
        gd.cornerRadius = roundRadius.toFloat()
        gd.setStroke(strokeWidth, strokeColor)
        return gd
    }

    fun customShape(fillColor: Int, strokeColor: Int, strokeWidth: Int, lt: Float, lb: Float, rt: Float, rb: Float): GradientDrawable {
        val gd = GradientDrawable() // 创建drawable
        gd.setColor(fillColor)
        gd.setStroke(strokeWidth, strokeColor)
        setGdCorner(gd, lt, lb, rt, rb)
        return gd
    }

    fun setGdCorner(gd: GradientDrawable, lt: Float, lb: Float, rt: Float, rb: Float) {
        gd.cornerRadii = arrayOf(lt, lt, rt, rt, lb, lb, rb, rb).toFloatArray()
    }

    fun getDrawable(context: Context, resId: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, resId, context.theme)
    }
}
