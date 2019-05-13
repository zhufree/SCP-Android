package info.free.scp.bean

import androidx.room.Entity

/**
 * 储存本地的收藏读过等信息
 */
@Entity(tableName = "LikeAndReadTable")
data class ScpInfoModel(var link: String, var title: String,
                        // 上次阅读的历史时间/加入待读列表的时间,根据type判断
                        var viewListType: Boolean,
                        // 待读还是读过的type，用一个表
                        var viewTime: Boolean)