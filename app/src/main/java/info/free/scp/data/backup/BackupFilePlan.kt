package info.free.scp.data.backup

import info.free.scp.SCPConstants.APP_PREF_NAME
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.LEVEL_PREF_NAME
import java.io.File

data class BackupFileSpec(
    val fileName: String,
    val mimeType: String,
    val localPath: String
)

class BackupFilePlan(
    privateDatabaseDirectory: String,
    privatePreferenceDirectory: String
) {
    val files = listOf(
        BackupFileSpec(
            fileName = INFO_DB_NAME,
            mimeType = "*/*",
            localPath = File(privateDatabaseDirectory, INFO_DB_NAME).path
        ),
        BackupFileSpec(
            fileName = LEVEL_PREF_NAME,
            mimeType = "application/xml",
            localPath = File(privatePreferenceDirectory, LEVEL_PREF_NAME).path
        ),
        BackupFileSpec(
            fileName = APP_PREF_NAME,
            mimeType = "application/xml",
            localPath = File(privatePreferenceDirectory, APP_PREF_NAME).path
        )
    )
}
