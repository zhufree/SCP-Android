package info.free.scp.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 储存本地的收藏读过等信息
 */
@Serializable
@Entity(tableName = "LaterAndHistoryTable")
data class ScpInfoModel(@PrimaryKey var link: String, var title: String,
                        // 上次阅读的历史时间/加入待读列表的时间,根据type判断
                        var viewListType: Int = -1,
                        // 待读还是读过的type，用一个表
                        var viewTime: Int = -1)