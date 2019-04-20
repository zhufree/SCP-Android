package info.free.scp.bean

import androidx.room.Entity
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * for detail
 */
@Serializable
@Entity(tableName="details")
data class ScpDetail(var link: String, var detail: String,
                     @Optional @SerialName("download_type") var downloadType:Int,
                     @Optional @SerialName("not_found") var notFound: Int = -1)