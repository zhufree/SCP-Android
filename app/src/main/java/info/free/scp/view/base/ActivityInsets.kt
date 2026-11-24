package info.free.scp.view.base

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import info.free.scp.util.ThemeUtil

/**
 * targetSdk 35+ 在 Android 15+ 强制 edge-to-edge，window.statusBarColor 和 navigationBarColor 不再生效。
 * 这里把内容留在系统栏内侧，并用窗口背景给状态栏区域补上标题栏颜色，其余区域用页面底色。
 */
internal fun AppCompatActivity.keepContentInsideSystemBars() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

    val background = SystemBarsBackground()
    window.setBackgroundDrawable(background)
    refreshSystemBars()

    val content = findViewById<View>(android.R.id.content)
    val initialLeft = content.paddingLeft
    val initialTop = content.paddingTop
    val initialRight = content.paddingRight
    val initialBottom = content.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            left = initialLeft + bars.left,
            top = initialTop + bars.top,
            right = initialRight + bars.right,
            bottom = initialBottom + bars.bottom
        )
        background.topInset = bars.top
        insets.inset(bars.left, bars.top, bars.right, bars.bottom)
    }
    ViewCompat.requestApplyInsets(content)
}

/** 切换主题后刷新系统栏背景色和图标深浅；Android 15 以下仍由 window.statusBarColor 负责。 */
internal fun AppCompatActivity.refreshSystemBars() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

    val controller = WindowCompat.getInsetsController(window, window.decorView)
    // 标题栏在日夜两种主题下都是深色，状态栏图标始终用浅色；导航栏随页面底色
    controller.isAppearanceLightStatusBars = false
    controller.isAppearanceLightNavigationBars = ThemeUtil.currentTheme != ThemeUtil.NIGHT_THEME
    // 部分厂商系统（如 MIUI）在手势导航下仍读取导航栏颜色，并会叠加对比度遮罩
    window.navigationBarColor = ThemeUtil.containerBg
    window.isNavigationBarContrastEnforced = false
    window.decorView.invalidate()
}

private class SystemBarsBackground : Drawable() {
    var topInset = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }
    private val paint = Paint()

    override fun draw(canvas: Canvas) {
        val area = bounds
        paint.color = ThemeUtil.containerBg
        canvas.drawRect(area, paint)
        paint.color = ThemeUtil.toolbarBg
        canvas.drawRect(
            area.left.toFloat(),
            area.top.toFloat(),
            area.right.toFloat(),
            (area.top + topInset).toFloat(),
            paint
        )
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
