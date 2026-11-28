package info.free.scp.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class CatalogDateTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val shanghai = TimeZone.getTimeZone("Asia/Shanghai")
    private val losAngeles = TimeZone.getTimeZone("America/Los_Angeles")

    private fun version(exportedAt: String) = CatalogVersion("export", exportedAt, "1")

    @Test
    fun formatsUnixSecondsAndIsoAsTheSameDate() {
        assertEquals("2026-11-24", catalogDate(version("1795521600"), utc))
        assertEquals("2026-11-24", catalogDate(version("2026-11-24T12:00:00Z"), utc))
        assertEquals("2026-11-24", catalogDate(version("2026-11-24T12:00:00.123456789Z"), utc))
    }

    @Test
    fun usesTheLocalTimeZone() {
        val late = version("2026-11-25T20:00:00Z")
        assertEquals("2026-11-26", catalogDate(late, shanghai))
        assertEquals("2026-11-25", catalogDate(late, losAngeles))
        assertEquals("2026-11-25", catalogDate(version("2026-11-26T02:00:00+08:00"), utc))
    }

    @Test
    fun hidesLegacyAndInvalidDates() {
        assertNull(catalogDate(CatalogVersion.Legacy, shanghai))
        listOf(
            "0001-01-01T00:00:00Z", "0", "-1", "", "not-a-date", "2026-02-30T00:00:00Z",
            "999999999999", "9223372036854775807", "99999999999999999999"
        ).forEach { assertNull(it, catalogDate(version(it), shanghai)) }
    }
}
