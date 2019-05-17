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
abstract class ScpModel(
        @PrimaryKey @ColumnInfo(name = "_id") var id: Int = -1,
        @ColumnInfo(name = "_index")
        var index: Int = -1, // 本地数据表中的次序
        var link: String = "", var title: String = "", // 都有的
        @ColumnInfo(name = "scp_type")
        var scpType: Int = -1,
        @ColumnInfo(name = "sub_scp_type")
        var subScpType: String? = "",
        @ColumnInfo(name = "download_type")
        var downloadType: Int = -1,
        var author: String? = "", // 部分有的
//        @Ignore var tags: String = "", // 部分有的
//                    var isCollection: Int = 0,
        @Ignore var hasRead: Int = 0, @Ignore var like: Int = 0 // 预留：读过，收藏
)