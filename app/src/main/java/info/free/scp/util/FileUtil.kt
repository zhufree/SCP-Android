package info.free.scp.util

import android.os.Environment
import info.free.scp.SCPConstants.AppMode.OFFLINE
import info.free.scp.SCPConstants.DETAIL_DB_NAME
import info.free.scp.ScpApplication.Companion.context
import java.io.File


/**
 * Created by zhufree on 2018/11/22.
 * 数据文件操作工具类
 * scp_category_v2.db 目录文件，内置在asset中，第一次启动时从asset复制到app数据文件夹使用
 * scp_detail_v2.db 正文文档离线，下载后放在Documents/scp_download文件夹，检测并复制到app数据文件夹使用
 * 用户数据scp_info.db和level.xml也备份到documents/scp_download文件夹
 */

object FileUtil {

    val absPath = Environment.getDataDirectory().absolutePath
    val pkgName = context.packageName
    val sp = File.separator
    val privatePrefDirPath = "$absPath${sp}data$sp$pkgName${sp}shared_prefs$sp" // 'data/data/info.free.scp/shared_prefs/'
    val privateDbDirPath = "$absPath${sp}data$sp$pkgName${sp}databases$sp"


    fun checkDetailDb() {
        val dbReady = checkDataReady(DETAIL_DB_NAME)
        if (dbReady) {
            PreferenceUtil.setAppMode(OFFLINE)
            return
        }
    }


    /**
     * 检查私有数据库中db是否存在
     */
    fun checkDataReady(dbName: String): Boolean {
        return context.getDatabasePath(dbName).isFile
    }

}
