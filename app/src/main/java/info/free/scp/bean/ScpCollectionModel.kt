package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Entity(tableName = "scp_collection")
data class ScpCollectionModel(var creator: String = "", // 部分有的
                              var subtext: String = "", var snippet: String = "",
                              var desc: String = "",
                              @ColumnInfo(name = "sub_links") var subLinks: String = "" // 设定
) : ScpModel()