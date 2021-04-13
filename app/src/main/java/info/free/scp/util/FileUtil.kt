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

object FileUtil {

    private var fileUtil: FileUtil? = null
    val absPath = Environment.getDataDirectory().absolutePath
    val pkgName = context.packageName
    val sp = File.separator
    val privatePrefDirPath = "$absPath${sp}data$sp$pkgName${sp}shared_prefs$sp" // 'data/data/info.free.scp/shared_prefs/'
    val privateDbDirPath = "$absPath${sp}data$sp$pkgName${sp}databases$sp"


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
     * 检查私有数据库中scp_data.db是否存在
     */
    fun checkDataReady(dbName: String): Boolean {
        val backUpFile = File("$privateDbDirPath$dbName")
        return backUpFile.exists()
    }

}
