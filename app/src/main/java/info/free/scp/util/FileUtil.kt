package info.free.scp.util

import android.content.Context
import android.os.Environment
import android.util.Log
import info.free.scp.ScpApplication
import info.free.scp.db.ScpDatabase
import org.jetbrains.anko.*
import java.io.File


/**
 * Created by zhufree on 2018/11/22.
 * 备份工具类
 * 下载的数据库scp_data.db,用户数据scp_info.db和level.xml放在documents文件夹
 * 数据库直接下到documents然后复制到私有文件夹
 * scp_info.db和level.xml在每次备份时复制到documents
 */

class FileUtil(val mContext: Context) {

    companion object {
        private var fileUtil: FileUtil? = null
        var prefFilename = "level.xml"
        var infoDBFilename = "scp_info.db"
        var dataDbFilename = "scp_data.db"
        val appFolderName = "scp_download"
        val absPath = Environment.getDataDirectory().absolutePath
        val pkgName = ScpApplication.context.packageName
        val sp = File.separator
        val privatePrefDirPath = "$absPath${sp}data$sp$pkgName${sp}shared_prefs$sp" // 'data/data/info.free.scp/shared_prefs/'
        val privateDbDirPath = "$absPath${sp}data$sp$pkgName${sp}databases$sp"
        val documentDirPath = "${Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS)}$sp$appFolderName$sp"

        fun getInstance(context: Context): FileUtil {
            return fileUtil ?: FileUtil(context)
        }
    }

    /**
     * 根据文件夹路径+文件名凑出备份文件全路径 Documents/scp_download
     * @param fileName String
     * @return String
     */
    private fun getBackUpFilePath(fileName: String): String {
        checkBackupDir()
        return "$documentDirPath$fileName"
    }

    /**
     * 检查备份文件是否存在，如果没有就创建
     */
    private fun checkBackupDir() {
        val documentDir = File(documentDirPath)
        if (!documentDir.exists()) {
            documentDir.mkdirs()
        }
    }

    /**
     * 检查私有数据库中scp_data.db是否存在
     */
    fun checkDataReady(): Boolean {
        val backUpFile = File("$privateDbDirPath$dataDbFilename")
        return backUpFile.exists()
    }

    fun checkBackupDataExist(): Boolean {
        val backUpFile = File(getBackUpFilePath(dataDbFilename))
        return backUpFile.exists() &&
                backUpFile.lastModified() > PreferenceUtil.getServerLastUpdateTime()
                && backUpFile.length() > 50 * 1000 * 1000
    }

    /**
     * 检查scp_info.db或pref文件有无备份在Documents/scp_download文件夹
     */
    fun checkBackUpFileExist(fileName: String): Boolean {
        val backUpFile = File(getBackUpFilePath(fileName))
        return backUpFile.exists()
    }

    /**
     * TODO
     * 复制pref/db文件到备份文件夹
     * [filePath] String
     * [backup] 备份还是恢复操作
     */
    private fun copyFile(fromFilePath: String, toFilePath: String) {
        Log.i("freescp", fromFilePath)
        if (fromFilePath.isEmpty() || toFilePath.isEmpty()) return
        try {
            val fromFile = File(fromFilePath)
            val toFile = File(toFilePath)
            fromFile.copyTo(toFile, true)
//            if (isPref) {
//                val prefFile = File(filePath)
//                val backUpFile = File(getBackUpFilePath(prefFilename))
//                if (backup && prefFile.exists()) {
//                    prefFile.copyTo(backUpFile, true) // 复制到备份文件夹
//                    return
//                }
//                if (!backup && backUpFile.exists()) {
//                    backUpFile.copyTo(prefFile, true) // 从备份文件夹恢复
//                }
//            } else {
//                val dbFile = File(filePath)
//                val backUpFile = File(getBackUpFilePath(dataDbFilename))
//                if (backup && dbFile.exists()) {
//                    dbFile.copyTo(backUpFile, true) // 复制到备份文件夹
//                    return
//                }
//                if (!backup && backUpFile.exists()) {
//                    backUpFile.copyTo(dbFile, true) // 从备份文件夹恢复
//                }
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.toast("文件复制出错：" + e.message)
        }
    }


    /**
     * 恢复数据的Dialog
     */
    fun restoreDB() {
        val backUpFile = File(getBackUpFilePath(dataDbFilename))
        if (backUpFile.exists()) {
            mContext.alert("检测到本设备有之前保存的用户数据，是否恢复", "恢复") {
                yesButton {
                    mContext.toast("开始恢复")
                    doAsync {
                        if (restore()) {
                            ScpDatabase.getNewInstance()
                            uiThread {
                                mContext.toast("恢复完成")
                            }
                        }
                    }
                }
                noButton { }
            }.show()
        }
    }

    /**
     * 恢复数据库
     */
    private fun restore(): Boolean {
        try {
            copyFile(getBackUpFilePath(dataDbFilename), "$privateDbDirPath$dataDbFilename")
            copyFile(getBackUpFilePath(infoDBFilename), "$privateDbDirPath$infoDBFilename")
            copyFile(getBackUpFilePath(prefFilename), "$privatePrefDirPath$prefFilename")
            return true
        } catch (e: Exception) {
            mContext.toast("文件复制出错：" + e.message)
        }
        return false
    }

    fun restoreData(): Boolean {
        try {
            copyFile(getBackUpFilePath(dataDbFilename), "$privateDbDirPath$dataDbFilename")
            return true
        } catch (e: Exception) {
            mContext.toast("文件复制出错：" + e.message)
        }
        return false
    }
}
