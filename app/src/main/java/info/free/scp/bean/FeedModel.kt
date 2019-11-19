package info.free.scp.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedModel(var title: String = "", var link: String = "",
                     @SerialName("created_time") var createdTime: String? = "",
                     var rank: String = "",
                     @SerialName("comment_count") var commentCount: Int? = 0)