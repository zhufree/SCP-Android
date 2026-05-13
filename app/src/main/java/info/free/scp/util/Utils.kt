@file:Suppress("SpellCheckingInspection")

package info.free.scp.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import info.free.scp.SCPConstants.PUBLIC_DIR_NAME
import info.free.scp.ScpApplication
import info.free.scp.ScpApplication.Companion.currentActivity
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat.LONG
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat

object Utils {

    fun getScreenHeight(context: Activity): Int {
        val metric = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metric)
        return metric.heightPixels
    }

    fun getScreenWidth(context: Context): Int {
        val metric = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metric)
        return metric.widthPixels
    }

    fun dp2px(dpValue: Int, context: Context = ScpApplication.context): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dp(pxValue: Int, context: Context = ScpApplication.context): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun enabledWifi(context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return wifiManager != null && wifiManager.wifiState == WifiManager.WIFI_STATE_ENABLED
    }

    fun enabledNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null) {
                return networkInfo.isAvailable
            }
        } else {
            val network = connectivityManager.activeNetwork ?: return false
            val status = connectivityManager.getNetworkCapabilities(network) ?: return false
            if (status.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                return true
            }
        }
        return false
    }

    fun onlyEnabled4G(context: Context): Boolean {
        return enabledNetwork(context) && !enabledWifi(context)
    }

    fun formatDate(time: Long): String {
        val format = SimpleDateFormat.getDateTimeInstance(SHORT, LONG)
        return format.format(time)
    }

    fun formatNow() = formatDate(System.currentTimeMillis())

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun copyPrivateFileToPublic(
        context: Context,
        orgFilePath: String,
        displayName: String,
        mimeType: String
    ) {
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, displayName)
            put(MediaStore.Files.FileColumns.TITLE, displayName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/" + PUBLIC_DIR_NAME
            )
        }
        val resolver = context.contentResolver
        val insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        runCatching {
            File(orgFilePath).inputStream().use { inputStream ->
                insertUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    fun saveBitmapFile(bitmap: Bitmap, filename: String): File {
        val safeFilename = filename.ifBlank { "scp_image" }
        val privateFile = privatePictureDir(ScpApplication.context, "$safeFilename.jpg")
        privateFile.parentFile?.mkdirs()
        BufferedOutputStream(FileOutputStream(privateFile)).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveBitmapToMediaStore(bitmap, "$safeFilename.jpg")
            } else {
                saveBitmapToLegacyPublicDir(bitmap, safeFilename)
            }
        } catch (e: Exception) {
            Log.e("scp", "Failed to export image", e)
        }
        return privateFile
    }

    private fun saveBitmapToLegacyPublicDir(bitmap: Bitmap, filename: String) {
        val albumDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "SCP"
        )
        if (!albumDir.exists() && !albumDir.mkdirs()) {
            Log.e("scp", "Directory not created")
            return
        }
        val publicFile = File(albumDir, "$filename.jpg")
        BufferedOutputStream(FileOutputStream(publicFile)).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
    }

    private fun saveBitmapToMediaStore(bitmap: Bitmap, displayName: String) {
        val contentResolver = ScpApplication.context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SCP")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        var uri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ScpApplication.context.grantUriPermission(
                    ScpApplication.context.packageName,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let { imageUri ->
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, values, null, null)
            }
        } catch (e: Exception) {
            uri?.let { contentResolver.delete(it, null, null) }
            throw e
        }
    }

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

    fun getUrlIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    fun openUrl(url: String) {
        val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        currentActivity?.startActivity(Intent.createChooser(viewIntent, "Open with"))
    }

    fun copy(context: Context, text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager?.setPrimaryClip(clipData)
    }

    fun share(file: File, activity: AppCompatActivity) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    activity,
                    activity.applicationContext.packageName + ".provider",
                    file
                )
            )
        } else {
            shareIntent.putExtra(Intent.EXTRA_STREAM, file.toUri())
        }
        activity.startActivity(Intent.createChooser(shareIntent, "Share"))
    }
}
