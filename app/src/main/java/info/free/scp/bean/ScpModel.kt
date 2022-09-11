package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Serializable
@Entity(tableName = "scps")
open class ScpModel @Ignore constructor(
        @PrimaryKey @ColumnInfo(name = "_id") var id: Int = -1,
        @ColumnInfo(name = "_index")
        var index: Int = -1, // 本地数据表中的次序
        var link: String = "", var title: String = "", // 都有的
        @ColumnInfo(name = "scp_type")
        @SerializedName("scp_type")
        var scpType: Int = -1,
        var author: String? = "" // 部分有的
) {
        constructor() : this(-1,-1, "", "", -1,  "")
}