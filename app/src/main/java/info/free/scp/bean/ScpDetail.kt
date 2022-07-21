package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName

/**
 * for detail
 */
@Entity(tableName = "scp_detail")
data class ScpDetail(@PrimaryKey @ColumnInfo var link: String, var detail: String?,
                     @SerialName("not_found") @ColumnInfo(name = "not_found") var notFound: Int? = -1,
                     var tags: String? = ""
                     )