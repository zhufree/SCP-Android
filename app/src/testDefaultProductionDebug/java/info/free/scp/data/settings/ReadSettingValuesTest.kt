package info.free.scp.data.settings

import info.free.scp.SCPConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReadSettingValuesTest {
    @Test
    fun hanzTypeReadsLegacyStringValues() {
        // 旧版 DropDownPreference 存的是字符串 "0"/"1"
        assertEquals(SCPConstants.SIMPLE, FakeSharedPreferences(mapOf(KEY_DETAIL_HANZ_TYPE to "0")).readHanzType())
        assertEquals(SCPConstants.TRADITIONAL, FakeSharedPreferences(mapOf(KEY_DETAIL_HANZ_TYPE to "1")).readHanzType())
    }

    @Test
    fun hanzTypeAcceptsIntAndFallsBackToSimplified() {
        assertEquals(SCPConstants.TRADITIONAL, FakeSharedPreferences(mapOf(KEY_DETAIL_HANZ_TYPE to 1)).readHanzType())
        // 从未设置过：按简体（旧 UI 默认值显示“繁体”但从未落盘，旧代码缺省为 0）
        assertEquals(SCPConstants.SIMPLE, FakeSharedPreferences().readHanzType())
        assertEquals(SCPConstants.SIMPLE, FakeSharedPreferences(mapOf(KEY_DETAIL_HANZ_TYPE to "abc")).readHanzType())
        assertEquals(SCPConstants.SIMPLE, FakeSharedPreferences(mapOf(KEY_DETAIL_HANZ_TYPE to 7)).readHanzType())
    }

    @Test
    fun hanzTypeIsWrittenAsStringAndRoundTrips() {
        val prefs = FakeSharedPreferences()
        prefs.edit().putHanzType(SCPConstants.TRADITIONAL).apply()
        assertEquals("1", prefs.values[KEY_DETAIL_HANZ_TYPE])
        assertEquals(SCPConstants.TRADITIONAL, prefs.readHanzType())
        prefs.edit().putHanzType(SCPConstants.SIMPLE).apply()
        assertEquals("0", prefs.values[KEY_DETAIL_HANZ_TYPE])
        assertEquals(SCPConstants.SIMPLE, prefs.readHanzType())
    }

    @Test
    fun textSizeParsesStoredValuesAndSnapsToNearest() {
        assertEquals(12, storedToTextSize("12px"))
        assertEquals(20, storedToTextSize("20px"))
        assertEquals(16, storedToTextSize(null))
        assertEquals(16, storedToTextSize(""))
        assertEquals(16, storedToTextSize("big"))
        assertEquals(18, storedToTextSize("19px"))
        assertEquals(20, storedToTextSize("40px"))
        assertEquals("14px", textSizeToStored(14))
        DETAIL_TEXT_SIZES.forEach { assertEquals(it, storedToTextSize(textSizeToStored(it))) }
    }

    @Test
    fun userCodeIsGeneratedOnceAndStaysStable() {
        val prefs = FakeSharedPreferences()
        val first = prefs.stableUserCode(Random(1))
        assertTrue(first in 0 until USER_CODE_BOUND)
        // 之后即便换了随机源也不变
        repeat(5) { assertEquals(first, prefs.stableUserCode(Random(it + 100))) }
        assertEquals(first, prefs.values[KEY_USER_CODE])
    }

    @Test
    fun userCodeKeepsExistingValueIncludingZero() {
        assertEquals(0, FakeSharedPreferences(mapOf(KEY_USER_CODE to 0)).stableUserCode(Random(5)))
        assertEquals(321, FakeSharedPreferences(mapOf(KEY_USER_CODE to 321)).stableUserCode(Random(5)))
    }

    @Test
    fun keysKeepLegacyNames() {
        assertEquals("read_settings", READ_SETTINGS_FILE)
        assertEquals("hide_finished_article", KEY_HIDE_FINISHED)
        assertEquals("detail_text_size", KEY_DETAIL_TEXT_SIZE)
        assertEquals("detail_hanz_type", KEY_DETAIL_HANZ_TYPE)
    }
}
