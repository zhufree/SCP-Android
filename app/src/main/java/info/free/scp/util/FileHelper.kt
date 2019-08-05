package info.free.scp.util

import android.content.Context
import android.os.Environment
import android.util.Log
import info.free.scp.ScpApplication
import info.free.scp.db.ScpDatabase
import org.jetbrains.anko.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * Created by zhufree on 2018/11/22.
 * 备份工具类
 */

class FileHelper(val mContext: Context) {

    var prefFilename = "level.xml" // 用户名和等级信息
    val backUpFolderName = "backup"
    //    val appFolderName = "SCP"
    val cacheDir = mContext.cacheDir.absolutePath + sp // 'data/data/info.free.scp/cache/'

    val documentDir = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}$sp$appFolderName$sp"
    // 'data/data/info.free.scp/databases/'

    companion object {
        private var fileHelper: FileHelper? = null
        var infoDBFilename = "scp_info.db"
        var dataDbFilename = "scp_data.db"
        val absPath = Environment.getDataDirectory().absolutePath
        val pakName = ScpApplication.context.packageName
        val sp = File.separator
        val appFolderName = "scp_download"
        val prefDir = "$absPath${sp}data$sp$pakName${sp}shared_prefs$sp" // 'data/data/info.free.scp/shared_prefs/'
        val dbDir = "$absPath${sp}data$sp$pakName${sp}databases$sp"
        val documentPath = "${Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS)}$sp$appFolderName"
        fun getInstance(context: Context): FileHelper {
            return fileHelper ?: FileHelper(context)
        }
    }

    private fun getBackUpFileName(fileName: String): String {
        checkBackupDir()
        return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}" +
                "$sp$appFolderName$sp$fileName"
    }

    /**
     * 检查备份文件是否存在，如果没有就创建
     */
    private fun checkBackupDir() {
        val backupDir = File(documentPath)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }

    /**
     * 检查scp_data.db是否存在
     */
    fun checkDataExist(): Boolean {
        val backUpFile = File(dbDir + dataDbFilename)
        return backUpFile.exists()
    }

    /**
     * 刚下载完，从缓存文件夹复制到数据库文件夹
     * @param dbName String 数据库名
     * @param backup Boolean 是备份还是恢复
     */
    fun copyDataBaseFile(dbName: String, backup: Boolean) {
        Log.i("freescp", dbName)
        if (dbName.isEmpty()) return
        // /data/data/packageName/databases
        try {
            val cacheFile = File(cacheDir + dbName)
            val destFile = File(dbDir + dataDbFilename)
            val backUpFile = File(getBackUpFileName(dataDbFilename))
            if (backup && cacheFile.exists()) {
                cacheFile.copyTo(destFile, true) // 复制到数据库
                cacheFile.copyTo(backUpFile, true) // 复制到备份文件夹
                return
            }
            if (!backup && backUpFile.exists()) {
                backUpFile.copyTo(destFile, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.toast("文件复制出错：" + e.message)
        }
    }

    fun copyStreamToData(inputStream: FileInputStream) {
        try {
            val destFile = File(dbDir + dataDbFilename)
            val outputStream = FileOutputStream(destFile)
            inputStream.copyTo(outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.toast("文件复制出错：" + e.message)
        }

    }

    /**
     * 复制pref文件到备份文件夹
     * @param prefName String
     */
    private fun copyPrefFile(prefName: String, backup: Boolean) {
        Log.i("freescp", prefName)
        if (prefName.isEmpty()) return
        // /data/data/packageName/shared_prefs
        try {
            val prefFile = File(prefDir + prefName)
            val backUpFile = File(getBackUpFileName(prefName))
            if (backup && prefFile.exists()) {
                prefFile.copyTo(backUpFile, true) // 复制到备份文件夹
                return
            }
            if (!backup && backUpFile.exists()) {
                backUpFile.copyTo(prefFile, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.toast("文件复制出错：" + e.message)
        }
    }

    /**
     * 复制数据库文件到备份文件夹
     * @param dbName String
     */
    private fun copyDbFile(dbName: String, backup: Boolean) {
        Log.i("freescp", dbName)
        if (dbName.isEmpty()) return
        // /data/data/packageName/shared_prefs
        try {
            val dbFile = File(dbDir + dbName)
            val backUpFile = File(getBackUpFileName(dbName))
            val documentFile = File(documentDir + dbName)
            if (backup && dbFile.exists()) {
                dbFile.copyTo(backUpFile, true) // 复制到备份文件夹
                return
            }
//            if (!backup && backUpFile.exists()) {
//                backUpFile.copyTo(dbFile, true)
//            }
            if (!backup && documentFile.exists()) {
                documentFile.copyTo(dbFile, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.toast("文件复制出错：" + e.message)
        }
    }

    fun checkBackupDataExist(): Boolean {
        val backUpFile = File(getBackUpFileName(dataDbFilename))
        return backUpFile.exists() &&
                backUpFile.lastModified() > PreferenceUtil.getServerLastUpdateTime(-1)
//                && backUpFile.length() > 90 * 1000 * 1000
    }

    /**
     * 恢复数据的Dialog
     */
    fun restoreDB() {
        val backUpFile = File(getBackUpFileName(dataDbFilename))
        if (backUpFile.exists()) {
            mContext.alert("检测到备份数据库，是否恢复", "恢复") {
                yesButton {
                    mContext.toast("开始恢复")
                    doAsync {
                        if (restore()){
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
            copyDbFile(dataDbFilename, false)
            copyDbFile(infoDBFilename, false)
            copyPrefFile(prefFilename, false)
            return true
        } catch (e: Exception) {
            mContext.toast("文件复制出错：" + e.message)
        }
        return false
    }

    fun restoreData(): Boolean {
        try {
            copyDbFile(dataDbFilename, false)
            return true
        } catch (e: Exception) {
            mContext.toast("文件复制出错：" + e.message)
        }
        return false
    }
}
