package info.free.scp.util

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import android.util.Log
import info
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.SCPConstants.SCP_DB_NAME
import info.free.scp.ScpApplication.Companion.context
import info.free.scp.db.AppInfoDatabase
import info.free.scp.db.ScpDatabase
import org.jetbrains.anko.*
import java.io.*


/**
 * Created by zhufree on 2018/11/22.
 * 数据文件操作工具类
 * scp_category_v2.db 目录文件，内置在asset中，第一次启动时从asset复制到app数据文件夹使用
 * scp_detail_v2.db 正文文档离线，下载后放在Documents/scp_download文件夹，检测并复制到app数据文件夹使用
 * 用户数据scp_info.db和level.xml也备份到documents/scp_download文件夹
 */

class FileUtil(private val mContext: Context) {

    companion object {
        private var fileUtil: FileUtil? = null
        var prefFilename = "level.xml"
        var infoDBFilename = "scp_info.db"
        val appFolderName = "scp_download"
        val absPath = Environment.getDataDirectory().absolutePath
        val pkgName = context.packageName
        val sp = File.separator
        val privatePrefDirPath = "$absPath${sp}data$sp$pkgName${sp}shared_prefs$sp" // 'data/data/info.free.scp/shared_prefs/'
        val privateDbDirPath = "$absPath${sp}data$sp$pkgName${sp}databases$sp"
        val documentDirPath = "${
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS)
        }$sp$appFolderName$sp"

        fun getInstance(context: Context): FileUtil {
            return fileUtil ?: FileUtil(context)
        }
    }


    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    @Throws(IOException::class)
    fun copyCategoryDb() {
        val dbReady = checkDataReady(SCP_DB_NAME)
        if (dbReady) return
        val am: AssetManager = context.assets
        val inp: InputStream = am.open(SCP_DB_NAME)
        val dirPath = File(privateDbDirPath)
        dirPath.mkdir()
        val outFileName = privateDbDirPath + SCP_DB_NAME
        // Open the empty db as the output stream
        val oup = FileOutputStream(outFileName)
        inp.copyTo(oup)
        oup.close()
        inp.close()
    }

    fun checkDetailDb() {
        val dbReady = checkDataReady(DETAIL_DB_NAME)
        info("dbReady: $dbReady")
        if (dbReady) {
            PreferenceUtil.setAppMode(OFFLINE)
            return
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
    fun checkDataReady(dbName: String): Boolean {
        val backUpFile = File("$privateDbDirPath$dbName")
        return backUpFile.exists()
    }


    /**
     * 检查scp_info.db或pref文件有无备份在Documents/scp_download文件夹
     */
    private fun checkBackupFileExist(fileName: String): Boolean {
        val file = File(getBackUpFilePath(fileName))
        return file.exists()
    }

    /**
     * 复制pref/db文件到备份文件夹
     * [filePath] String
     * [backup] 备份还是恢复操作
     */
    private fun copyFile(fromFilePath: String, toFilePath: String) {
        Log.i("freescp", fromFilePath)
        if (fromFilePath.isEmpty() || toFilePath.isEmpty()) return
        val fromFile = File(fromFilePath)
        val toFile = File(toFilePath)
        if (fromFile.exists()) {
            fromFile.copyTo(toFile, true)
        }
    }


    /**
     * 恢复数据的Dialog
     */
    fun showRestore() {
        if (checkBackupFileExist(infoDBFilename)) {
            mContext.alert("检测到本设备有之前保存的用户数据，是否恢复", "恢复") {
                yesButton {
                    mContext.toast("开始恢复")
                    doAsync {
                        if (restore()) {
                            ScpDatabase.getNewInstance()
                            AppInfoDatabase.getNewInstance()
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
            copyFile(getBackUpFilePath(DETAIL_DB_NAME), "$privateDbDirPath$DETAIL_DB_NAME")
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
            copyFile(getBackUpFilePath(DETAIL_DB_NAME), "$privateDbDirPath$DETAIL_DB_NAME")
            return true
        } catch (e: Exception) {
            mContext.toast("文件复制出错：" + e.message)
        }
        return false
    }

    fun backup(): Boolean {
        try {
            copyFile("$privateDbDirPath$DETAIL_DB_NAME", getBackUpFilePath(DETAIL_DB_NAME))
            copyFile("$privateDbDirPath$infoDBFilename", getBackUpFilePath(infoDBFilename))
            copyFile("$privatePrefDirPath$prefFilename", getBackUpFilePath(prefFilename))
            return true
        } catch (e: Exception) {
            mContext.toast("文件复制出错：" + e.message)
        }
        return false
    }
}
