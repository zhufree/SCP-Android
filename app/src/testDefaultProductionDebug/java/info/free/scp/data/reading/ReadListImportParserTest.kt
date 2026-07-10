package info.free.scp.data.reading

import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadListImportParserTest {
    @Test
    fun parsesAsciiCommaSeparatedScpNumbers() {
        val result = ReadListImportParser.parse("scp-1, scp-23")

        assertEquals(
            listOf(
                ReadListImportRequest(SAVE_SERIES, "%-001 %"),
                ReadListImportRequest(SAVE_SERIES, "%-023 %")
            ),
            result
        )
    }

    @Test
    fun parsesCnAndJokeTypesWithChineseSeparators() {
        val result = ReadListImportParser.parse("SCP-CN-12；SCP-CN-J-3")

        assertEquals(
            listOf(
                ReadListImportRequest(SAVE_SERIES_CN, "%-012 %"),
                ReadListImportRequest(SAVE_JOKE_CN, "%-003-%")
            ),
            result
        )
    }

    @Test
    fun parsesWhitespaceSeparatedJokeNumbers() {
        val result = ReadListImportParser.parse("scp-j-7\nscp-j-8")

        assertEquals(
            listOf(
                ReadListImportRequest(SAVE_JOKE, "%-007-%"),
                ReadListImportRequest(SAVE_JOKE, "%-008-%")
            ),
            result
        )
    }

    @Test
    fun ignoresTokensWithoutNumbers() {
        val result = ReadListImportParser.parse("hello, world")

        assertEquals(emptyList<ReadListImportRequest>(), result)
    }
}
