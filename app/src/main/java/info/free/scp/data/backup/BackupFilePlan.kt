package info.free.scp.data.backup

import info.free.scp.SCPConstants.APP_PREF_NAME
import info.free.scp.SCPConstants.INFO_DB_NAME
import info.free.scp.SCPConstants.LEVEL_PREF_NAME

data class BackupFileSpec(
    val fileName: String,
    val mimeType: String,
    val localPath: String,
    val sidecarPaths: List<String> = emptyList()
)

class BackupFilePlan(
    privateDatabaseDirectory: String,
    privatePreferenceDirectory: String
) {
    val files = listOf(
        BackupFileSpec(
            fileName = INFO_DB_NAME,
            mimeType = "*/*",
            localPath = "$privateDatabaseDirectory$INFO_DB_NAME",
            sidecarPaths = listOf(
                "$privateDatabaseDirectory$INFO_DB_NAME-shm",
                "$privateDatabaseDirectory$INFO_DB_NAME-wal"
            )
        ),
        BackupFileSpec(
            fileName = LEVEL_PREF_NAME,
            mimeType = "application/xml",
            localPath = "$privatePreferenceDirectory$LEVEL_PREF_NAME"
        ),
        BackupFileSpec(
            fileName = APP_PREF_NAME,
            mimeType = "application/xml",
            localPath = "$privatePreferenceDirectory$APP_PREF_NAME"
        )
    )
}
