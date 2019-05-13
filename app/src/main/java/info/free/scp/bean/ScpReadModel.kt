package info.free.scp.bean

import androidx.room.Entity

/**
 * 储存本地的收藏读过等信息
 */
@Entity(tableName = "LaterAndHistoryTable")
data class ScpReadModel (var link: String, var title: String, var like: Boolean, var hasRead: Boolean)