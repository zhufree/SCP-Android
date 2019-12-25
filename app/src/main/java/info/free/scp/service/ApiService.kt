package info.free.scp.service

import info.free.scp.PrivateConstants
import info.free.scp.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Created by Free on 2018/5/5.
 * retrofit接口
 */

interface ApiService {

    @GET("/latest_cn/{pageIndex}")
    suspend fun getLatestCn(@Path("pageIndex") pageIndex: Int): ApiBean.ApiListResponse<FeedModel>

    @GET("/latest_translated/{pageIndex}")
    suspend fun getLatestTranslated(@Path("pageIndex") pageIndex: Int): ApiBean.ApiListResponse<FeedModel>

    @GET("/top_rated_all/{pageIndex}")
    suspend fun getTopRated(@Path("pageIndex") pageIndex: Int): ApiBean.ApiListResponse<FeedModel>

    @GET("/top_rated_scp/{pageIndex}")
    suspend fun getTopRatedScp(@Path("pageIndex") pageIndex: Int): ApiBean.ApiListResponse<FeedModel>

    @GET("/top_rated_tale/{pageIndex}")
    suspend fun getTopRatedTale(@Path("pageIndex") pageIndex: Int): ApiBean.ApiListResponse<FeedModel>

    @GET("/top_rated_goi")
    suspend fun getTopRatedGoi(): ApiBean.ApiListResponse<FeedModel>

    @GET("/top_rated_wanderers/{pageIndex}")
    suspend fun getTopRatedWander(@Path("pageIndex") pageIndex: Int): ApiBean.ApiListResponse<FeedModel>

    @Headers(
            PrivateConstants.APP_ID,
            PrivateConstants.API_KEY,
            PrivateConstants.CONTENT_TYPE_JSON
    )
    @GET("1/classes/Config")
    fun getAppConfig(): Observable<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>
}