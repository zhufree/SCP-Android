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
}
