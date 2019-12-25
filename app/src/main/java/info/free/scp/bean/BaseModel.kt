package info.free.scp.bean

import androidx.room.PrimaryKey

open class BaseModel(@PrimaryKey var objectId: String = "", var createdAt: String = "",
                     var updatedAt: String = "") : ApiBean.ApiResponse()