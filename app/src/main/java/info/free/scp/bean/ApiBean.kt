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
}