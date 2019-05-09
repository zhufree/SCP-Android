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

@Serializable
@Entity(tableName = "scps")
data class ScpModel(
        @PrimaryKey @ColumnInfo(name = "_id") var id: Int = -1,
        @Optional @SerialName("_index") @ColumnInfo(name = "_index")
        var index: Int = -1, // 本地数据表中的次序
//                    val createdAt: String, val updatedAt: String,  // Bmob自带字段
        @Optional var link: String = "", @Optional var title: String = "", // 都有的
//                    @Optional @SerialName("detail") var detailHtml: String = "", // 预留保存正文
//                    @Optional @SerialName("not_found") var notFound: Int = -1,
        @Optional @SerialName("scp_type") @ColumnInfo(name = "scp_type")
        var scpType: Int = -1,
        @Optional @SerialName("sub_scp_type") @ColumnInfo(name = "sub_scp_type")
        var subScpType: String? = "",
        @Optional @SerialName("download_type") @ColumnInfo(name = "download_type")
        var downloadType: Int = -1,
        @Ignore @Optional var subtext: String = "",
        @Ignore @Optional var snippet: String = "",
        @Ignore @Optional var desc: String = "", // 设定
        @Optional var author: String? = "", // 部分有的
        @Ignore @Optional var creator: String = "", // 部分有的
        @Optional @SerialName("created_time") @ColumnInfo(name = "created_time")
        var createdTime: String? = "", // 基金会故事创建时间
//                    @Optional @SerialName("page_code") var pageCode: String = "", // 基金会故事按字母排序
//                    @Optional @SerialName("contest_name") var contestName: String = "",
//                    @Optional @SerialName("contest_link") var contestLink: String = "", // 征文竞赛
//                    @Optional @SerialName("event_type") var eventType: String = "",
//                    @Optional val month: String = "",
        @Ignore @Optional var tags: String = "", // 部分有的
        @Optional @SerialName("sub_links") @ColumnInfo(name = "sub_links")
        @Ignore var subLinks: String = "", // collection
//                    @Optional var isCollection: Int = 0,
        @Optional @Ignore var hasRead: Int = 0, @Optional @Ignore var like: Int = 0 // 预留：读过，收藏
)