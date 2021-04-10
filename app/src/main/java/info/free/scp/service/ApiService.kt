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

    @GET("/latest_index/")
    suspend fun getLatestIndex(): FeedIndexModel

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
    
    @GET("/get_detail/{link}") // 不带/
    suspend fun getDetail(@Path("link") link: String): ApiBean.ApiListResponse<String>

    @GET("/get_comment/{link}") // 不带/
    suspend fun getComment(@Path("link") link: String): ApiBean.ApiListResponse<CommentModel>

    @GET("/get_random/{typeRange}")
    suspend fun getRandom(@Path("typeRange") typeRange: String): ApiBean.ApiListResponse<ScpItemModel>

    @GET("/get_sibling_scp/{direct}/{index}/{scpType}")
    suspend fun getSiblingScp(@Path("direct") direct: String,
                              @Path("index") index: Int,
                              @Path("scpType") scpType: Int
    ): ApiBean.ApiListResponse<ScpItemModel>

    @GET("/get_sibling_collection/{direct}/{index}/{scpType}")
    suspend fun getSiblingCollection(@Path("direct") direct: String,
                                     @Path("index") index: Int,
                                     @Path("scpType") scpType: Int
    ): ApiBean.ApiListResponse<ScpCollectionModel>

    @GET("/get_direct/{scpType}/{numberString}")
    suspend fun getDirect(@Path("scpType") scpType: Int,
                          @Path("numberString") numberString: String)
            : ApiBean.ApiListResponse<ScpItemModel>

    @Headers(
            PrivateConstants.APP_ID,
            PrivateConstants.API_KEY,
            PrivateConstants.CONTENT_TYPE_JSON
    )
    @GET("1/classes/Config")
    fun getAppConfig(): Observable<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>
}