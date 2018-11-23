package info.free.scp.util

import android.content.Context
import android.content.DialogInterface
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.io.IOException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by zhufree on 2018/11/22.
 * 备份工具类
 */

class BackupHelper {
    private var dbFilename = "scp_info.db"
    private var fileList: Array<String?>? = null // 数据库文件列表
    private var choicePosition = -3 // 选择数据库列表中的位置
    private var dialog: AlertDialog? = null
    private val BACK_FOLDER = "backup"
    private val appName = "SCP"
    private lateinit var mContext: Context

    fun BackupAndRestore(context: Context) {
        mContext = context
    }

    /**
     * 恢复数据的Dialog
     */
    fun restoreDB() {
        fileList = getFileList()
        val builder = AlertDialog.Builder(mContext)
        builder.setIcon(android.R.drawable.ic_dialog_info)
        builder.setTitle("恢复")
        builder.setSingleChoiceItems(getFileList(), -1, DialogClick())
        builder.setPositiveButton("确定", DialogClick())
        builder.setNegativeButton("取消", DialogClick())
        builder.show()
    }

    /**
     * 备份数据库
     */
    fun backupDB() {
        showDialog("是否备份数据库", 'B')
    }

    /**
     * 显示一个Dialog
     *
     * @param title
     * 标题 ，必须引用资源ID resource ID
     * @param sign
     * 根据标示调用方法 I - 恢复默认设置 D - 恢复默认设置 H -选择主机
     */
    private fun showDialog(title: String, sign: Char) {
        AlertDialog.Builder(mContext).setTitle(title)
                .setPositiveButton("确定") { _, _ ->
                    when (sign) {
                        'B' // 备份数据库
                        -> {
                            if (dialog == null) {
                                dialog = awaitDialog(mContext)
                            } else {
                                dialog?.show()
                            }
                            Flowable.create<String>({ emitter ->
                                Log.i("search", "开始备份")
                                backUp()
                                emitter.onNext("finish")
                                emitter.onComplete()
                            }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { _ ->
                                        if (dialog != null) {
                                            dialog!!.dismiss()
                                        }
                                    }
                        }
                        else -> {
                        }
                    }
                }.setNegativeButton("取消") { _, _ ->
                }.show()
    }

    /**
     * 备份操作
     *
     * @return
     */
    private fun backUp(): Boolean {
        var isOk = false
        val sp = File.separator
        val bkFolderFile = sdCardOk()
        if (bkFolderFile != null) {
            try {
                // 在backup文件夹下创建备份文件
                val dbFile = dbOk(dbFilename)
                dbFile?.let {
                    val backDbFile = File(bkFolderFile.absolutePath + sp
                            + it.name)
                    backDbFile.createNewFile()
                    isOk = fileCopy(backDbFile, it.absoluteFile)
                    return isOk
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return isOk
    }

    /**
     * 文件夹列表
     *
     * @return
     */
    private fun getFileList(): Array<String?>? {
        var fileList: Array<String?>? = null
        val file = sdCardOk()
        if (file != null) {
            val list = file.listFiles()
            if (list != null && list.isNotEmpty()) {
                fileList = arrayOfNulls(list.size)
                for (i in list.indices) {
                    fileList[i] = list[i].name
                }
            }
        }
        return fileList
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
            val backUpPath = "${Environment.getExternalStorageDirectory()}$sp$appName$sp$BACK_FOLDER"
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
     *
     * @param name
     * 选择的文件名称 选中的数据库名称
     * @param resoreDbName
     * 需要恢复的数据库名称
     * @return
     */
    fun restore(name: String, f: File?): Boolean {
        var isOk = false
        if (f != null) {
            val dbFile = dbOk(name)
            try {
                // System.out.println("覆盖的名称"+dbName);
                if (dbFile != null) {
                    isOk = fileCopy(dbFile, f.absoluteFile)
                } else
                    isOk = false
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
        val sp = File.separator
        val absPath = Environment.getDataDirectory().absolutePath
        val pakName = mContext.packageName
        // /data/data/packageName/databases
        val dbPath = ("$absPath${sp}data$sp$pakName${sp}databases$sp$dbName")
        return File(dbPath)
    }

    /**
     * 等候动画
     */
    fun awaitDialog(context: Context): AlertDialog {
        val bar = ProgressBar(context)
        bar.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setCancelable(false)
        dialog.show()
        val window = dialog.window
        val params = window.attributes
        params.width = 50
        params.height = 50
        window.attributes = params
        window.setContentView(bar)
        return dialog
    }

    /**
     * 复制文件
     * @param outFile
     * 写入
     * @param inFile
     * 读取
     * @throws FileNotFoundException
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

    private inner class DialogClick : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, which: Int) {
            if (which == -1) { // 确定
                if (choicePosition < 0) {
                    Toast.makeText(mContext, "选择数据库", Toast.LENGTH_SHORT)
                            .show()
                    return
                }
                val sp = File.separator
                val folderName = fileList!![choicePosition]
                val backUpPath = "${Environment.getExternalStorageDirectory()}$sp$appName$sp$BACK_FOLDER$sp$folderName"
                val file = File(backUpPath)
                if (file.isDirectory) {
                    val files = file.listFiles()
                    var isOk = false
                    for (i in files.indices) {
                        val f = files[i]
                        isOk = restore(f.name, f)
                        if (!isOk) {
                            val failMsg = "恢复失败" + ":" + f.getName()
                            Toast.makeText(mContext, failMsg,
                                    Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    if (isOk) {
                        // 如果有数据体现则需要刷新出新的数据

                    }
                }
            } else if (which == -2) { // 取消
            } else if (which >= 0) {
                choicePosition = which
            }
        }
    }
}
