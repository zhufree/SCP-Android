package info.free.scp.data.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class DetailTitleFormatterTest {
    @Test
    fun screenshotFileNameRemovesAsciiSpaces() {
        assertEquals(
            "SCP-173-雕像",
            DetailTitleFormatter.toScreenshotFileName("SCP-173 - 雕像")
        )
    }

    @Test
    fun screenshotFileNamePreservesNonSpaceCharacters() {
        assertEquals(
            "SCP-中文：测试",
            DetailTitleFormatter.toScreenshotFileName("SCP-中文：测试")
        )
    }

    @Test
    fun screenshotFileNameKeepsEmptyTitleEmpty() {
        assertEquals("", DetailTitleFormatter.toScreenshotFileName(""))
    }
}
