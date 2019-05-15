package info.free.scp.util

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.io.IOException
import org.jetbrains.anko.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.channels.FileChannel


/**
 * Created by zhufree on 2018/11/22.
 * 备份工具类
 */

class FileHelper(val mContext: Context) {
    private var dbFilename = "scp_info.db"
    private var newDbFilename = "scp_data.db"
    private var prefFilename = "level.xml" // 用户名和等级信息
    private val backUpFolderName = "backup"
    private val appFolderName = "SCP"
    private val sp = File.separator
    private val absPath = Environment.getDataDirectory().absolutePath
    private val pakName = mContext.packageName
    private val cacheDir = mContext.cacheDir.absolutePath + sp // 'data/data/info.free.scp/cache/'
    private val dbDir = "$absPath${sp}data$sp$pakName${sp}databases$sp"
    // 'data/data/info.free.scp/databases/'

    companion object {
        private var fileHelper: FileHelper? = null
        fun getInstance(context: Context): FileHelper {
            return fileHelper ?: FileHelper(context)
        }
    }

    private fun getBackUpFileName(): String {
        return "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp$backUpFolderName$sp$dbFilename"
    }

    /**
     * 从缓存文件夹复制到数据库文件夹
     */
    fun copyDataBaseFile(dbName: String) {
        Log.i("freescp", dbName)
        if (dbName.isEmpty()) return
        // /data/data/packageName/databases
        try {
            val cacheFile = File(cacheDir + dbName)
            val destFile = File(dbDir + newDbFilename)
            val backUpFile = File(getBackUpFileName())
            cacheFile.copyTo(destFile, true) // 复制到数据库
            cacheFile.copyTo(backUpFile, true) // 复制到备份文件夹
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.toast("文件复制出错：" + e.message)
        }
    }

    /**
     * 恢复数据的Dialog
     */
    fun restoreDB() {
        val backUpFile = File(getBackUpFileName())
        if (backUpFile.exists()) {
            mContext.alert("检测到备份数据库，是否恢复", "恢复") {
                yesButton {
                    mContext.toast("开始恢复")
                    doAsync {
                        val resultMsg = if (restore()) "恢复完成" else "恢复失败"
                        mContext.toast(resultMsg)
                    }
                }
                noButton { }
            }.show()
        }
    }

    /**
     * 备份数据库
     */
    fun backupDB() {
        AlertDialog.Builder(mContext)
                .setTitle("是否备份数据库")
                .setMessage("包括文档和读过、收藏数据以及职位，编号，等级等信息")
                .setPositiveButton("确定") { _, _ ->
                    Toaster.show("开始备份")
                    Flowable.create<Boolean>({ emitter ->
                        emitter.onNext(backUp())
                        emitter.onComplete()
                    }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                if (it) {
                                    Toaster.show("备份完成")
                                } else {
                                    Toaster.show("备份失败，请检查SD卡读取权限。")
                                }
                            }
                }
                .setNegativeButton("取消") { _, _ -> }
                .show()
    }

    /**
     * 备份操作
     *
     * @return
     */
    fun backUp(): Boolean {
        var isOk = false
        val sp = File.separator
        val bkFolderFile = sdCardOk()
        if (bkFolderFile != null) {
            try {
                // 在backup文件夹下创建备份文件
                val dbFile = dbOk(dbFilename)
                val prefFile = prefOk(prefFilename)
                dbFile?.let {
                    val backDbFile = File(bkFolderFile.absolutePath + sp
                            + it.name)
                    backDbFile.deleteOnExit()
                    backDbFile.createNewFile()
                    isOk = fileCopy(backDbFile, it.absoluteFile)
                }
                prefFile?.let {
                    val backPrefFile = File(bkFolderFile.absolutePath + sp
                            + it.name)
                    backPrefFile.deleteOnExit()
                    backPrefFile.createNewFile()
                    isOk = fileCopy(backPrefFile, it.absoluteFile)
                }
                return isOk
            } catch (e: FileNotFoundException) {
                isOk = false
            } catch (e: IOException) {
                isOk = false
            }

        }
        return isOk
    }

    /**
     * sdCard是否存在，创建备份的文件夹并返回
     *
     * @return null不能使用
     */
    private fun sdCardOk(): File? {
        var bkFolderFile: File? = null
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val sp = File.separator
            // 外部存储路径/SCP/backup/
            val backUpPath = "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp$backUpFolderName"
            bkFolderFile = File(backUpPath)
            if (!bkFolderFile.exists()) {
                bkFolderFile.mkdirs()
            } else
                return bkFolderFile
        } else {
            Toast.makeText(mContext, "sd卡不存在，请检查sd卡权限", Toast.LENGTH_SHORT).show()
        }
        return bkFolderFile
    }

    /**
     * 恢复数据库
     */
    fun restore(): Boolean {
        var isOk = false
        val backUpDbPath = "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp" +
                "$backUpFolderName$sp$dbFilename"
        val backUpPrefPath = "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp" +
                "$backUpFolderName$sp$prefFilename"
        val backUpDbFile = File(backUpDbPath)
        val backUpPrefFile = File(backUpPrefPath)
        try {
            backUpDbFile?.let {
                val dbFile = dbOk(it.name)
                dbFile?.let { _ ->
                    mContext.deleteDatabase(dbFilename)
                    isOk = fileCopy(dbFile, it.absoluteFile)
                }

            }
            backUpPrefFile?.let {
                val prefFile = prefOk(it.name)
                prefFile?.let { _ ->
                    mContext.deleteDatabase(prefFilename)
                    isOk = fileCopy(prefFile, it.absoluteFile)
                }
            }
        } catch (e: FileNotFoundException) {
            isOk = false
        } catch (e: IOException) {
            isOk = false
        }
        return isOk
    }

    /**
     * 数据库文件是否存在，并可以使用
     *
     * @return 数据库文件本身
     */
    private fun dbOk(dbName: String): File? {
        val absPath = Environment.getDataDirectory().absolutePath
        val pakName = mContext.packageName
        // /data/data/packageName/databases
        val dbPath = ("$absPath${sp}data$sp$pakName${sp}databases$sp$dbName")
        return File(dbPath)
    }

    /**
     * pref文件是否存在，并可以使用
     *
     * @return 数据库文件本身
     */
    private fun prefOk(prefName: String): File? {
        val absPath = Environment.getDataDirectory().absolutePath
        val pakName = mContext.packageName
        // /data/data/packageName/shared_prefs
        val dbPath = ("$absPath${sp}data$sp$pakName${sp}shared_prefs$sp$prefName")
        return File(dbPath)
    }

    fun checkBackUpFileExist(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            // 外部存储路径/SCP/backup/
            val backUpFilePath = "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp$backUpFolderName$sp$dbFilename"
            val bkDbFile = File(backUpFilePath)
            return bkDbFile.exists()
        } else {
            Toast.makeText(mContext, "sd卡不存在，请检查sd卡权限", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    /**
     * 复制文件
     */
    @Throws(IOException::class, FileNotFoundException::class)
    private fun fileCopy(outFile: File?, inFile: File?): Boolean {
        if (outFile == null || inFile == null) {
            return false
        }
        var isOk = true
        val inChannel: FileChannel = FileInputStream(inFile).channel // 只读
        val outChannel: FileChannel = FileOutputStream(outFile).channel // 只写
        val size = inChannel.transferTo(0, inChannel.size(), outChannel)
        if (size <= 0) {
            isOk = false
        }
        inChannel.close()
        outChannel.close()
        return isOk
    }
}
