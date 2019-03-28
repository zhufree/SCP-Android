@file:Suppress("SpellCheckingInspection")

package info.free.scp.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.IOException
import okhttp3.MediaType
import okhttp3.RequestBody
import android.util.DisplayMetrics
import android.content.ContentUris
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.os.Build
import android.content.ContentValues
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import info.free.scp.SCPConstants
import info.free.scp.ScpApplication
import java.io.*
import java.text.DateFormat.*
import java.text.SimpleDateFormat


object Utils {

    /**
     * 获取屏幕宽高
     */
    fun getScreenHeight(context: Activity): Int {
        val metric = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metric)
        return metric.heightPixels
    }

    fun getScreenWidth(context: Activity): Int {
        val metric = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metric)
        return metric.widthPixels
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dp2px(dpValue: Int, context: Context = ScpApplication.context): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dp(pxValue: Int, context: Context = ScpApplication.context): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * wifi开启检查
     *
     * @return
     */
    fun enabledWifi(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifiManager != null && wifiManager.wifiState == WifiManager.WIFI_STATE_ENABLED
    }

    /**
     * 3G网开启检查
     *
     * @return
     */
    fun enabledNetwork(context: Context): Boolean {
        val cManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cManager?.activeNetworkInfo != null && cManager.activeNetworkInfo.isAvailable
    }
    fun onlyEnabled4G(context: Context): Boolean {
        return enabledNetwork(context) && !enabledWifi(context)
    }

    private fun formatDate(time: Long): String {
        val format =  SimpleDateFormat.getDateTimeInstance(SHORT, LONG)
        return format.format(time)
    }

    fun formatNow() = formatDate(System.currentTimeMillis())

    /**
     * bitmap保存为一个文件
     * @param bitmap bitmap对象
     * @return 文件对象
     */
    fun saveBitmapFile(bitmap: Bitmap, filename: String): File {
        val filePath = getAlbumStorageDir("SCP").path + "/$filename"
        val file = File("$filePath.jpg")
        try {
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val contentResolver = ScpApplication.context.contentResolver
        val values = ContentValues(4)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.ORIENTATION, 0)
        values.put(MediaStore.Images.Media.TITLE, "scp_donation")
        values.put(MediaStore.Images.Media.DESCRIPTION, "scp_donation")
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
        var url: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ScpApplication.context.grantUriPermission(ScpApplication.context.packageName,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            url = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            //其实质是返回 Image.Media.DATA中图片路径path的转变而成的uri
            val imageOut = contentResolver?.openOutputStream(url)
            imageOut?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, imageOut)
            }

            val id = ContentUris.parseId(url)
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND,
                    null)//获取缩略图

        } catch (e: Exception) {
            if (url != null) {
                contentResolver?.delete(url, null, null)
            }
        }
        return file
    }

    fun save(file: File, filename: String) {
        val filePath = getAlbumStorageDir("SCP").path + "/$filename"
        val newFile = File("$filePath.jpg")
        if (!newFile.exists()) {
            newFile.createNewFile()
        }
        try {
            val outputStream = BufferedOutputStream(FileOutputStream(newFile))
            outputStream.write(file.readBytes())
            outputStream.close()
        } catch (e: FileNotFoundException) {
            Toaster.show("未开启SD卡读取权限，需要手动开启。")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getAlbumStorageDir(albumName: String): File {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs() && !file.exists()) {
            Log.e("scp", "Directory not created")
        }
        return file
    }

    /**
     * 保存gif文件，单独处理
     */
    fun saveGifFile(bytes: ByteArray, fileName: String) {
        val destFile = File(fileName)
        try {
            val outputStream = FileOutputStream(destFile, false)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 通过Uri获取文件
     */
    fun getFileByUri(uri: Uri, context: Context): File? {
        var path: String? = MiPictureHelper.getPath(context, uri)
        return File(path)
//        if (!TextUtils.isEmpty(uri.authority)) {
//            val cursor = context?.contentResolver?.query(uri,
//                    arrayOf(MediaStore.Images.Media.DATA),null, null, null);
//            if (null == cursor) {
//                Toaster.show("图片未找到")
//                return null
//            }
//            cursor.moveToFirst()
//            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
//            cursor.close()
//            return File(path)
//        } else {
//            path = uri.path
//        }
//        when (uri.scheme) {
//            "file" -> {
//                path = uri.encodedPath
//                if (path != null) {
//                    path = Uri.decode(path)
//                    val contentResolver = context.contentResolver
//                    val builder = StringBuilder()
//                    builder.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
//                            .append("'").append(path).append("'").append(")")
//                    val cur = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                            arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA),
//                            builder.toString(), null, null)
//                    cur?.moveToFirst()
//                    while (!cur.isAfterLast) {
//                        path = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
//                        cur.moveToNext()
//                    }
//                    cur.close()
//                }
//                return File(path)
//            }
//            "content" -> {
//                // 4.2.2以后
//                val projection = arrayOf(MediaStore.Images.Media.DATA)
//                val cursor = context.contentResolver.query(uri, projection, null, null, null)
//                if (cursor != null && cursor.moveToFirst()) {
//                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//                    path = cursor.getString(columnIndex)
//                    cursor.close()
//                }
//                return File(path)
//            }
//        }
//        return null
    }

    /**
     * 把[file]转换成二进制流传输，别的方法都上传失败
     */
    fun fileToByteBody(file: File): RequestBody {
        val fis = FileInputStream(file)
        val bos = ByteArrayOutputStream()
        val b = ByteArray(1024)
        var n = fis.read(b)
        while (n != -1) {
            bos.write(b, 0, n)
            n = fis.read(b)
        }
        fis.close()
        bos.close()
        return RequestBody.create(MediaType.parse("application/octet-stream"), bos.toByteArray())
    }

    fun getDownloadTitleByType(downloadType: Int): String{
        return when (downloadType) {
            SCPConstants.Download.DOWNLOAD_SCP -> "SCP系列"
            SCPConstants.Download.DOWNLOAD_SCP_CN -> "SCP-CN系列"
            SCPConstants.Download.DOWNLOAD_TALE -> "基金会故事"
            SCPConstants.Download.DOWNLOAD_ARCHIVES -> "其他文档"
            SCPConstants.Download.DOWNLOAD_COLLECTIONS -> "故事系列，设定中心，正文竞赛等"
            else -> "正文"
        }
    }
}