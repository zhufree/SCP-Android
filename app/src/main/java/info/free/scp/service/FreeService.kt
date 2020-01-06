package info.free.scp.service

import info.free.scp.PrivateConstants
import info.free.scp.bean.ApiBean
import info.free.scp.bean.MealModel
import info.free.scp.bean.PortalModel
import retrofit2.http.*

interface FreeApi {
    @Headers(PrivateConstants.CONTENT_TYPE_JSON, PrivateConstants.APP_ID, PrivateConstants.API_KEY)
    @GET("1/classes/PortalModel")
    suspend fun getAllPortal(@Query("skip") skip: Int = 0,
                             @Query("order") order: String = "-createdAt")
            : ApiBean.ApiListResponse<PortalModel>

    @Headers(PrivateConstants.CONTENT_TYPE_JSON, PrivateConstants.APP_ID, PrivateConstants.API_KEY)
    @GET("1/classes/MealModel")
    suspend fun getAllMeal(@Query("skip") skip: Int = 0,
                           @Query("order") order: String = "order")
            : ApiBean.ApiListResponse<MealModel>
}

object FreeService : BaseService() {

    suspend fun getAllPortal(): ApiBean.ApiListResponse<PortalModel> {
        return ApiManager.portalApi.getAllPortal()
    }

    suspend fun getAllMeal(): ApiBean.ApiListResponse<MealModel> {
        return ApiManager.portalApi.getAllMeal()
    }
}