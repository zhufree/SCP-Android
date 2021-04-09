package info.free.scp.bean

import com.google.gson.annotations.SerializedName

data class FeedModel(var title: String = "", var link: String = "",
                     @SerializedName("created_time")
                     var createdTime: String = "",
                     var rank: String = "")