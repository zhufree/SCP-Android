package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.serialization.Serializable

/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Serializable
@Entity(tableName = "scps")
data class ScpItemModel @Ignore constructor(
        @ColumnInfo(name = "sub_scp_type")
        var subScpType: String? = "",
        @ColumnInfo(name = "created_time")
        var createdTime: String? = "" // 基金会故事创建时间
):ScpModel() {
        constructor() : this("","")
}