package info.free.scp.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 储存本地的收藏读过等信息
 */
@Serializable
@Entity(tableName = "LikeAndReadTable")
data class ScpLikeModel (@PrimaryKey var link: String = "", var title: String = "", var like: Boolean,
                         var hasRead: Boolean)