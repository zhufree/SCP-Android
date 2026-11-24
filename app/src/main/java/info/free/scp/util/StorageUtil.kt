package info.free.scp.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import info
import java.io.*


@RequiresApi(api = Build.VERSION_CODES.Q)
fun getPublicFileUri(context: Context, dirName: String, fileName: String, mimeType: String) {
    val values = ContentValues()
    values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
    values.put(MediaStore.Files.FileColumns.TITLE, fileName)
//        values.put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + dirName)
    val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val resolver = context.contentResolver
    val insertUri = resolver.insert(external, values)
    info(insertUri.toString())
}


fun privatePictureDir(context: Context, fileName: String): File {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File("${dir?.path}${File.separator}$fileName")
    return file
}

@Throws(IOException::class)
fun copyFileFromUri(context: Context, fileUri: Uri, outputFile: File): Boolean {
    val input = context.contentResolver.openInputStream(fileUri) ?: return false
    input.use { source -> outputFile.outputStream().use { source.copyTo(it) } }
    return true
}

@Throws(IOException::class)
fun copyFileToUri(context: Context, inputFile: File, outputUri: Uri) {
    inputFile.inputStream().use { source ->
        val output = context.contentResolver.openOutputStream(outputUri, "wt") ?: throw IOException("无法打开输出文件")
        output.use { source.copyTo(it) }
    }
}
