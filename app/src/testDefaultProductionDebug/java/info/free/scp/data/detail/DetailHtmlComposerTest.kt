package info.free.scp.data.detail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailHtmlComposerTest {
    @Test
    fun dayThemeUsesConfiguredTextSizeAndLightColors() {
        val html = DetailHtmlComposer("18px", initialNightTheme = false)
            .compose("<p>content</p>", nightTheme = false)

        assertTrue(html.contains("font-size:18px"))
        assertTrue(html.contains("background-color:#fff"))
        assertTrue(html.contains("<p>content</p>"))
        assertFalse(html.contains("darkreader.min.js"))
    }

    @Test
    fun nightThemeKeepsDarkStylesAndInitialDarkReaderScript() {
        val html = DetailHtmlComposer("16px", initialNightTheme = true)
            .compose("content", nightTheme = true)

        assertTrue(html.contains("background-color:#222"))
        assertTrue(html.contains("background:#333!important"))
        assertTrue(html.contains("darkreader.min.js"))
        assertTrue(html.indexOf("style.css") < html.indexOf("content"))
        assertTrue(html.indexOf("content") < html.indexOf("jquery-ui.min.js"))
    }

    @Test
    fun pageColorsAreInjectedAsBackgroundAndCssVariablesBeforeContent() {
        val colors = DetailPageColors(
            background = 0xFFF3F6F4.toInt(),
            primary = 0xFF1B5E3A.toInt(),
            primaryContainer = 0xFFC6E9D1.toInt(),
            surface = 0xFFFFFFFF.toInt(),
            outline = 0xFF717971.toInt(),
            outlineVariant = 0xFFC1C9C2.toInt(),
            onSurfaceVariant = 0xFF414942.toInt()
        )
        val html = DetailHtmlComposer("16px", initialNightTheme = false)
            .compose("<p>content</p>", nightTheme = false, pageColors = colors)

        assertTrue(html.contains("body{background-color:#F3F6F4;}"))
        assertTrue(html.indexOf("body{background-color:#F3F6F4;}") > html.indexOf("body{background-color:#fff;}"))
        assertTrue(html.contains("--scp-rate-primary:#1B5E3A;"))
        assertTrue(html.contains("--scp-rate-container:#C6E9D1;"))
        assertTrue(html.contains("--scp-rate-on-surface-variant:#414942;"))
        assertTrue(html.indexOf("--scp-rate-primary") < html.indexOf("<p>content</p>"))
    }

    @Test
    fun withoutPageColorsNothingIsInjected() {
        val html = DetailHtmlComposer("16px", initialNightTheme = false)
            .compose("<p>content</p>", nightTheme = false)

        assertFalse(html.contains("--scp-rate-"))
        assertTrue(html.contains("body{background-color:#fff;}"))
    }
}
