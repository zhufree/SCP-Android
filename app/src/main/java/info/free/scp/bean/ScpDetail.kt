package info.free.scp.bean

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName

/**
 * for detail
 */
data class ScpDetail(var link: String, var detail: String,
                     @Optional @SerialName("not_found") var notFound: Int = -1)