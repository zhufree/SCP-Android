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

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/SeriesFirstStory")
    fun getFirstStory(@Query("skip") skip: Int, @Query("limit") limit: Int): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/SeriesSecondStory")
    fun getSecondStory(@Query("skip") skip: Int, @Query("limit") limit: Int): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/SeriesThirdStory")
    fun getThirdStory(@Query("skip") skip: Int, @Query("limit") limit: Int): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpTalesModel")
    fun getScpTales(@Query("where") where: String): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpTalesCnModel")
    fun getCnScpTales(@Query("where") where: String): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/SeriesArchiveModel")
    fun getScpStorySeries(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 中国原创故事系列 60
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/StorySeriesCnModel")
    fun getCnStorySeries(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 设定中心 30
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpSettingModel")
    fun getScpSettings(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 设定中心CN 11
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpSettingCNModel")
    fun getCnScpSettings(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 征文 33
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpContestModel")
    fun getScpContest(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 征文CN 15
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpContestCnModel")
    fun getScpContestCn(): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 添加数据，直接传json字符串
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @POST("1/classes/EventModel")
    fun addNewEvent(@Body event: RequestBody): Observable<ApiBean.AddResponse>


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


}