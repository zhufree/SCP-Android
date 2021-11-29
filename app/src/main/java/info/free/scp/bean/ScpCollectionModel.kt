package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore


/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Entity(tableName = "scp_collection")
data class ScpCollectionModel @Ignore constructor(var creator: String? = "", // 部分有的
                              var subtext: String? = "", var snippet: String? = "",
                              var desc: String? = "",
                              @ColumnInfo(name = "sub_links") var subLinks: String? = "" // 设定
) : ScpModel() {
    constructor() : this("","", "", "", "")
}