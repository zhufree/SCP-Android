package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName

/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Entity(tableName = "scps")
data class ScpItemModel(
        @ColumnInfo(name = "sub_scp_type")
        var subScpType: String? = "",
        @ColumnInfo(name = "created_time")
        var createdTime: String? = "" // 基金会故事创建时间
):ScpModel()