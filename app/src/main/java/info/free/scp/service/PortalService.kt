package info.free.scp.service

import info.free.scp.PrivateConstants
import info.free.scp.bean.ApiBean
import info.free.scp.bean.PortalModel
import retrofit2.http.*

interface PortalApi {
    @Headers(PrivateConstants.CONTENT_TYPE_JSON, PrivateConstants.APP_ID, PrivateConstants.API_KEY)
    @GET("1/classes/PortalModel")
    suspend fun getAllPortal(@Query("skip") skip: Int = 0,
//                      @Query("limit") limit: Int = 100,
                             @Query("order") order: String = "-createdAt")
            : ApiBean.ApiListResponse<PortalModel>
//
//    @Headers(PrivateConstants.CONTENT_TYPE_JSON, PrivateConstants.APP_ID, PrivateConstants.API_KEY)
//    @GET("1/classes/PortalModel")
//    suspend fun getPortalOfAuthor(@Query("where") where: String, @Query("order") order: String = "-createdAt")
//            : ApiBean.ApiListResponse<PortalModel>
//
//    @Headers(PrivateConstants.CONTENT_TYPE_JSON, PrivateConstants.APP_ID, PrivateConstants.API_KEY)
//    @POST("1/classes/PortalModel")
//    suspend fun addNewPortal(@Body jsonStr: RequestBody): ApiBean.AddResponse

    // 获取单个model
//    @Headers(PrivateConstants.CONTENT_TYPE_JSON, PrivateConstants.APP_ID, PrivateConstants.API_KEY)
//    @GET("1/classes/PortalModel/{objectId}")
//    suspend fun getPortal(@Path("objectId") objectId: String): PortalModel
}

object PortalService : BaseService() {

    suspend fun getAllPortal(): ApiBean.ApiListResponse<PortalModel> {
        return ApiManager.portalApi.getAllPortal()
    }

//    suspend fun getPortalOfAuthor(authorId: String, next: (List<PortalModel>) -> Unit) {
//        val where = "{\"authorId\":\"portalId\"}"
//        val response = ApiManager.portalApi.getPortalOfAuthor(where)
//        next(response.results)
//    }
//
//    suspend fun addNewPortal(newPortal: PortalModel, next: () -> Unit) {
//        val portalStr = postGson.toJson(newPortal)
//        val portalBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), portalStr)
//        val response = ApiManager.portalApi.addNewPortal(portalBody)
//        next()
//    }
//
//    suspend fun getPortal(portalId: String, next: (PortalModel) -> Unit) {
//        next(ApiManager.portalApi.getPortal(portalId))
//    }
//
//    suspend fun deletePortal(portalId: String, next: () -> Unit) {
//        ApiManager.portalApi.deletePortal(portalId)
//        next()
//    }

}