package info.free.scp.bean

import androidx.room.ColumnInfo

/**
 * 用于列表展示，不带太多信息
 */
data class DisplayScpModel(@ColumnInfo(name="link") var link: String,
                           @ColumnInfo(name="title") var title: String)