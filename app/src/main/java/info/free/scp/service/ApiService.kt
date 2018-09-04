package info.free.scp.service

import info.free.scp.PrivateConstants
import info.free.scp.bean.ApiBean
import info.free.scp.bean.ScpModel
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by Free on 2018/5/5.
 * retrofit接口
 */

interface ApiService {
    // 获取全部数据
    // --data-urlencode 'limit=200' \
    // --data-urlencode 'skip=400' \
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/SeriesModel")
    fun getScpSeriesModel(@Query("skip") skip: Int, @Query("limit") limit: Int)
            : Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/SeriesCNModel")
    fun getSeriesCnModel(@Query("skip") skip: Int, @Query("limit") limit: Int)
            : Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/JokeScpCNModel")
    fun getJokeCnModel(): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/JokeScpModel")
    fun getJokeModel(): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ExScpModel")
    fun getExScp(): Observable<ApiBean.ApiListResponse<ScpModel>>


    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ArchivedScpModel")
    fun getArchivedScp(): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/RemovedScpModel")
    fun getRemovedScp(): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/DecommissionedScpModel")
    fun getDecommissionedScp(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 添加数据，直接传json字符串
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @POST("1/classes/EventModel")
    fun addNewEvent(@Body event: RequestBody): Observable<ApiBean.AddResponse>

    @Headers(
            "Content-Type:application/json;charset=UTF-8",
            "Accept: application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @POST("1/classes/PhotoModel")
    fun addNewPhoto(@Body jsonStr: RequestBody): Observable<ApiBean.AddResponse>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @POST("1/classes/ArticleModel")
    fun addNewArticle(@Body jsonStr: RequestBody): Observable<ApiBean.AddResponse>

    // 上传图片
    @Headers(
            "Content-Type:image/*",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @POST("2/files/{fileName}")
    fun uploadPhoto(@Path("fileName") fileName: String, @Body body: RequestBody)
            : Observable<ApiBean.UploadResponse>

    // 获取单个model
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/EventModel/{objectId}")
    fun getEvent(@Path("objectId") objectId: String): Observable<ScpModel>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/PhotoModel/{objectId}")
    fun getPhoto(@Path("objectId") objectId: String): Observable<ScpModel>


    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ArticleModel/{objectId}")
    fun getArticle(@Path("objectId") objectId: String): Observable<ScpModel>

    // 更新单个model
    /**
    {
    "updatedAt": "2018-06-07 14:37:03"
    }
     */
    @Headers(
            "Content-Type:application/json;charset=UTF-8",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @PUT("1/classes/EventModel/{objectId}")
    fun updateEvent(@Path("objectId") objectId: String, @Body jsonStr: RequestBody)
            : Observable<ApiBean.UpdateResponse>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @PUT("1/classes/PhotoModel/{objectId}")
    fun updatePhoto(@Path("objectId") objectId: String, @Body jsonStr: RequestBody)
            : Observable<ApiBean.UpdateResponse>


    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @PUT("1/classes/ArticleModel/{objectId}")
    fun updateArticle(@Path("objectId") objectId: String, @Body jsonStr: RequestBody)
            : Observable<ApiBean.UpdateResponse>

    // 删除单个model
    /**
    {
    "msg": "ok"
    }
     */
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @DELETE("1/classes/EventModel/{objectId}")
    fun deleteEvent(@Path("objectId") objectId: String): Observable<ApiBean.DeleteResponse>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:99b50c359acbb2320eb1f31cfbba9627",
            "X-Bmob-REST-API-Key:11cc24ba20f00493feae503a8e508f41"
    )
    @DELETE("1/classes/PhotoModel/{objectId}")
    fun deletePhoto(@Path("objectId") objectId: String): Observable<ApiBean.DeleteResponse>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:99b50c359acbb2320eb1f31cfbba9627",
            "X-Bmob-REST-API-Key:11cc24ba20f00493feae503a8e508f41"
    )
    @DELETE("1/classes/ArticleModel/{objectId}")
    fun deleteArticle(@Path("objectId") objectId: String): Observable<ApiBean.DeleteResponse>


}