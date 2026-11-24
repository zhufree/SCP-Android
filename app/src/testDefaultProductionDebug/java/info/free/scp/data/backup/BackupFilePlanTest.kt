package info.free.scp.data.backup

import info.free.scp.SCPConstants.APP_PREF_NAME
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.LEVEL_PREF_NAME
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupFilePlanTest {
    private val plan = BackupFilePlan(
        privateDatabaseDirectory = "/databases/",
        privatePreferenceDirectory = "/shared_prefs/"
    )

    @Test
    fun preservesBackupFileNamesAndOrder() {
        assertEquals(
            listOf(INFO_DB_NAME, LEVEL_PREF_NAME, APP_PREF_NAME),
            plan.files.map { it.fileName }
        )
    }

    @Test
    fun mapsDatabaseAndPreferencesToExistingPrivatePaths() {
        assertEquals(
            listOf(
                "/databases/$INFO_DB_NAME",
                "/shared_prefs/$LEVEL_PREF_NAME",
                "/shared_prefs/$APP_PREF_NAME"
            ),
            plan.files.map { it.localPath }
        )
    }

    @Test
    fun onlyPreferencesAreExportedAsXml() {
        assertEquals(listOf("*/*", "application/xml", "application/xml"), plan.files.map { it.mimeType })
    }
}
