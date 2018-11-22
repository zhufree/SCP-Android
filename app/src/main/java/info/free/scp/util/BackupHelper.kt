package info.free.scp.util

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.widget.ProgressBar



/**
 * Created by zhufree on 2018/11/22.
 * 备份工具类
 */

object BackupHelper {
    private var fileList: Array<String>? = null // 数据库文件列表
    private var choicePostion = -3 // 选择数据库列表中的位置
    private var dialog: AlertDialog? = null
    private val BACK_FOLDER = "backup"
    private val appName = "myApp"

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
                .setPositiveButton("确定", object : OnClickListener() {
                    fun onClick(dialogI: DialogInterface, which: Int) {
                        when (sign) {
                            'B' // 备份数据库
                            -> {
                                if (dialog == null) {
                                    dialog = awaitDialog(mContext)
                                } else {
                                    dialog!!.show()
                                }
                                ExecuteTask().execute('B')
                            }
                            else -> {
                            }
                        }
                    }
                }).setNegativeButton("取消", object : OnClickListener() {
                    fun onClick(dialog: DialogInterface, which: Int) {}
                }).show()
    }

    /**
     * 备份操作
     *
     * @return
     */
    private fun backUp(): Boolean {
        var isOk = false
        val sp = File.separator
        val sdFile = sdCardOk()
        if (sdFile != null) {
            try {
                val dbNames = arrayOf("数据库名称")
                // 创建日期文件夹
                val folder_date = datePrefix()
                val f = File(sdFile!!.getAbsolutePath() + sp + folder_date)
                if (!f.exists()) {
                    f.mkdirs()
                }
                for (i in dbNames.indices) {
                    val dbName = dbNames[i]
                    val dbFile = dbOk(dbName)
                    if (dbFile != null) {
                        val backFile = File(f.getAbsolutePath() + sp
                                + dbFile!!.getName())
                        backFile.createNewFile()
                        isOk = fileCopy(backFile, dbFile!!.getAbsoluteFile())
                        if (!isOk) {
                            break
                        }
                    }
                }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }
        return isOk
    }

    /**
     * 时间前缀
     *
     * @return
     */
    private fun datePrefix(): String {
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        val date = Date(System.currentTimeMillis())
        return format.format(date)
    }

    /**
     * 文件夹列表
     *
     * @return
     */
    private fun getFileList(): Array<String>? {
        var fileList: Array<String>? = null
        val file = sdCardOk()
        if (file != null) {
            val list = file!!.listFiles()
            if (list != null && list!!.size > 0) {
                fileList = arrayOfNulls(list!!.size)
                for (i in list!!.indices) {
                    fileList[i] = list!![i].getName()
                }
            }
        }
        return fileList
    }

    /**
     * sdCard是否存在 备份的文件夹是否存在
     *
     * @return null不能使用
     */
    private fun sdCardOk(): File? {
        var bkFile: File? = null
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            val sp = File.separator
            val backUpPath = (Environment.getExternalStorageDirectory() + sp
                    + appName + sp + BACK_FOLDER)
            bkFile = File(backUpPath)
            if (!bkFile!!.exists()) {
                bkFile!!.mkdirs()
            } else
                return bkFile
        } else
            Toast.makeText(mContext, "Sdcard 不存在", Toast.LENGTH_SHORT).show()
        return bkFile
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
                    isOk = fileCopy(dbFile, f!!.getAbsoluteFile())
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
     * @return
     */
    private fun dbOk(dbName: String): File? {
        val sp = File.separator
        val absPath = Environment.getDataDirectory().getAbsolutePath()
        val pakName = mContext!!.getPackageName()
        val dbPath = (absPath + sp + "data" + sp + pakName + sp + "databases"
                + sp + dbName)
        val file = File(dbPath)
        return if (file.exists()) {
            file
        } else {
            null
        }
    }

    /**
     * 等候动画
     */
    fun awaitDialog(context: Context?): AlertDialog {
        val bar = ProgressBar(context)
        bar.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT)
        val dialog = AlertDialog.Builder(context).create()
        dialog.setCancelable(false)
        dialog.show()
        val window = dialog.getWindow()
        val params = window.getAttributes()
        params.width = 50
        params.height = 50
        window.setAttributes(params)
        window.setContentView(bar)
        return dialog
    }

    /**
     *
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
        val inChannel = FileInputStream(inFile).getChannel() // 只读
        val outChannel = FileOutputStream(outFile).getChannel() // 只写
        try {
            val size = inChannel!!.transferTo(0, inChannel!!.size(), outChannel)
            if (size <= 0) {
                isOk = false
            }
        } catch (e: IOException) {
            isOk = false
            e.printStackTrace()
        } finally {
            if (inChannel != null) {
                inChannel!!.close()
            }
            if (outChannel != null) {
                outChannel!!.close()
            }
        }
        return isOk
    }

    private inner class DialogClick : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, which: Int) {
            if (which == -1) { // 确定
                if (choicePostion < 0) {
                    Toast.makeText(mContext, "选择数据库", Toast.LENGTH_SHORT)
                            .show()
                    return
                }
                val sp = File.separator
                val folderName = fileList!![choicePostion]
                val backUpPath = (Environment.getExternalStorageDirectory()
                        + sp + appName + sp + BACK_FOLDER + sp + folderName)
                val file = File(backUpPath)
                if (file.isDirectory()) {
                    val files = file.listFiles()
                    var isOk = false
                    for (i in files.indices) {
                        val f = files[i]
                        isOk = restore(f.getName(), f)
                        if (!isOk) {
                            val fail_msg = "恢复失败" + ":" + f.getName()
                            Toast.makeText(mContext, fail_msg,
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
                choicePostion = which
            }
        }
    }

    /**
     * 执行任务
     *
     * @author Administrator
     */
    private inner class ExecuteTask : AsyncTask<Char, Void, Boolean>() {
        protected override fun doInBackground(vararg params: Char): Boolean? {
            val c = params[0]
            if (c == 'B') {
                backUp()
            }
            return null
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (dialog != null) {
                dialog!!.dismiss()
            }
        }
    }
}
