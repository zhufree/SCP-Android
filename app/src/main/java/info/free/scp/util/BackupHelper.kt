package info.free.scp.util

import android.content.Context
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.io.IOException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * Created by zhufree on 2018/11/22.
 * 备份工具类
 */

class BackupHelper(val mContext: Context) {
    private var dbFilename = "scp_info.db"
    private var prefFilename = "level.xml" // 用户名和等级信息
    private val backUpFolderName = "backup"
    private val appFolderName = "SCP"
    private val sp = File.separator

    companion object {
        private var backupHelper: BackupHelper? = null
        fun getInstance(context: Context): BackupHelper {
            return backupHelper ?: BackupHelper(context)
        }
    }

    fun getBackUpFileName(): String {
        return "${Environment.getExternalStorageDirectory()}$sp$appFolderName$sp$backUpFolderName$sp$dbFilename"
    }
    /**
     * 恢复数据的Dialog
     */
    fun restoreDB() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("恢复")
        builder.setMessage("尝试从本地备份中恢复数据库")
        builder.setPositiveButton("确定") { _, _ ->
            Toaster.show("开始恢复")
            Flowable.create<Boolean>({ emitter ->
                emitter.onNext(restore())
                emitter.onComplete()
            }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val resultMsg = if (it) "恢复完成" else "恢复失败"
                        Toaster.show(resultMsg)
                    }
        }
        builder.setNegativeButton("取消"){_,_->}
        builder.show()
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
                    Flowable.create<String>({ emitter ->
                        backUp()
                        emitter.onNext("finish")
                        emitter.onComplete()
                    }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                Toaster.show("备份完成")
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
            } catch (e: IOException) {
                e.printStackTrace()
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
        backUpDbFile?.let {
            val dbFile = dbOk(it.name)
            try {
                dbFile?.let {_ ->
                    mContext.deleteDatabase(dbFilename)
                    isOk = fileCopy(dbFile, it.absoluteFile)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        backUpPrefFile?.let {
            val prefFile = prefOk(it.name)
            try {
                prefFile?.let { _ ->
                    mContext.deleteDatabase(prefFilename)
                    isOk = fileCopy(prefFile, it.absoluteFile)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
    @Throws(IOException::class)
    private fun fileCopy(outFile: File?, inFile: File?): Boolean {
        if (outFile == null || inFile == null) {
            return false
        }
        var isOk = true
        val inChannel = FileInputStream(inFile).channel // 只读
        val outChannel = FileOutputStream(outFile).channel // 只写
        try {
            val size = inChannel.transferTo(0, inChannel.size(), outChannel)
            if (size <= 0) {
                isOk = false
            }
        } catch (e: IOException) {
            isOk = false
            e.printStackTrace()
        } finally {
            inChannel?.close()
            outChannel?.close()
        }
        return isOk
    }
}
