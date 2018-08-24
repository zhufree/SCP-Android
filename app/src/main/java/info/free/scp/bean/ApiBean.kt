package info.zhufree.freelove67.bean

/**
 * Created by zhufree on 2018/5/23.
 * 解析获取列表的bean
 */

class ApiBean {
    data class ApiListResponse<T> (val results: List<T>)
    data class AddResponse (val createdAt: String, val objectId: String)
    data class UploadResponse(val filename: String, val url: String, val cdn: String)
    data class UpdateResponse(val updatedAt: String)
    data class DeleteResponse(val msg: String)
}