package info.free.scp.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName

/**
 * for detail
 */
@Entity(tableName = "scp_detail")
data class ScpDetail(@PrimaryKey var link: String, var detail: String,
                     @Optional @SerialName("not_found") @ColumnInfo(name = "not_found") var notFound: Int = -1)