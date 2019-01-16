package info.free.scp.bean

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by zhufree on 2018/8/24.
 * SCP数据Model
 */

@Serializable
data class ScpCollectionModel(@SerialName("objectId") val sId: String,
                              val createdAt: String, val updatedAt: String,  // Bmob自带字段
                              @Optional var link: String = "", @Optional var title: String = "", // 都有的
                              @Optional @SerialName("detail") var detailHtml: String = "", // 预留保存正文
                              @Optional @SerialName("not_found") var notFound: Int = -1,
                              @Optional @SerialName("download_type") var downloadType: Int = -1,
                              @Optional @SerialName("scp_type") var scpType: Int = -1,
                              @Optional var author: String = "",@Optional var creator: String = "", // 部分有的
                              @Optional var subtext: String = "", @Optional var snippet: String = "",
                              @Optional var desc: String = "",
                              @Optional @SerialName("links") var subLinks: String = "", // 设定
                              @Optional var index: Int = -1, // 本地数据表中的次序
                              @Optional var hasRead: Int = 0, @Optional var like: Int = 0, // 预留：读过，收藏
                              @Optional var saveType: String = "" // 本地存储用
)