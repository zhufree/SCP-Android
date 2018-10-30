package info.free.scp.util

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
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object Utils {
    /**
     * bitmap保存为一个文件
     * @param bitmap bitmap对象
     * @return 文件对象
     */
    fun saveBitmapFile(bitmap: Bitmap): File {
        val filePath = getAlbumStorageDir("SCP").path + "/img_donation"
        val file = File("$filePath.jpg")
        try {
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    private fun getAlbumStorageDir(albumName: String): File {
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
        var path: String? = null
        when (uri.scheme) {
            "file" -> {
                path = uri.encodedPath
                if (path != null) {
                    path = Uri.decode(path)
                    val contentResolver = context.contentResolver
                    val builder = StringBuilder()
                    builder.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                            .append("'").append(path).append("'").append(")")
                    val cur = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA),
                            builder.toString(), null, null)
                    cur?.moveToFirst()
                    while (!cur.isAfterLast) {
                        path = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                        cur.moveToNext()
                    }
                    cur.close()
                }
                return File(path)
            }
            "content" ->{
                // 4.2.2以后
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    path = cursor.getString(columnIndex)
                    cursor.close()
                }
                return File(path)
            }
        }
        return null
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
}