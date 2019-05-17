package info.free.scp.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 储存本地的收藏读过等信息
 */
@Entity(tableName = "LikeAndReadTable")
data class ScpLikeModel (@PrimaryKey var link: String = "", var title: String = "", var like: Boolean,
                         var hasRead: Boolean)