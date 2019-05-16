package info.free.scp.bean

import androidx.room.ColumnInfo

/**
 * 用于列表展示，不带太多信息
 */
data class SimpleScp(@ColumnInfo(name="link") var link: String,
                     @ColumnInfo(name="title") var title: String,
                     @ColumnInfo(name="viewTime") var viewTime: String,
                     @ColumnInfo(name="hasRead") var hasRead: Int = 0,
                     @ColumnInfo(name="like") var like: Int = 0)