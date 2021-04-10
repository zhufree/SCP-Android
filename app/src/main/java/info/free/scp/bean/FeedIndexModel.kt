package info.free.scp.bean

import com.google.gson.annotations.SerializedName

data class FeedIndexModel(
        @SerializedName("latest_create")
        var latestCreate: List<FeedModel>,
        @SerializedName("latest_translate")
        var latestTranslate: List<FeedModel>
)