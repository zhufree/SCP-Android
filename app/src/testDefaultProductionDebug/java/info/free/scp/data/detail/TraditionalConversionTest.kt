package info.free.scp.data.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import taobe.tec.jcc.JChineseConvertor

/** 详情页在“繁体”设置下用 JChineseConvertor.s2t 转换正文：确认转换库可用，且不破坏 HTML 标记。 */
class TraditionalConversionTest {
    private val converter = JChineseConvertor.getInstance()

    @Test
    fun convertsSimplifiedTextToTraditional() {
        assertEquals("隱藏已讀文檔", converter.s2t("隐藏已读文档"))
    }

    @Test
    fun keepsHtmlMarkupAndAsciiIntact() {
        val html = """<p class="scp-item">项目编号：SCP-173</p><a href="/scp-173">收容措施</a>"""
        val converted = converter.s2t(html)
        assertTrue(converted.contains("""<p class="scp-item">"""))
        assertTrue(converted.contains("""href="/scp-173""""))
        assertTrue(converted.contains("SCP-173"))
        assertTrue(converted.contains("項目編號"))
        assertTrue(converted.contains("收容措施"))
    }
}
