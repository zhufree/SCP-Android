package info.free.scp.bean

import androidx.room.Ignore
import kotlinx.serialization.Serializable

/**
 * Created by zhufree on 2018/5/23.
 * 解析获取列表的bean
 */

class ApiBean {
    data class ApiListResponse<T>(val results: List<T>) : ApiResponse()
    abstract class ApiResponse(@Ignore val error: String? = "", @Ignore val code: Int = 0)
    data class AddResponse (val createdAt: String, val objectId: String)
    data class UploadResponse(val filename: String, val url: String, val cdn: String)
    data class UpdateResponse(val updatedAt: String)
    data class DeleteResponse(val msg: String)
    @Serializable
    data class ConfigResponse(val key: String, val value: String, val createdAt: String,
                              val updatedAt: String, val objectId: String)
}