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
    // SCP系列
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModel")
    fun getSeries(@Query("where") where: String,
                  @Query("skip") skip: Int,
                  @Query("limit") limit: Int)
            : Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModel")
    fun getAllScp(@Query("skip") skip: Int,
                  @Query("limit") limit: Int)
            : Observable<ApiBean.ApiListResponse<ScpModel>>

    // 归档内容，带cn和type查询
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModel")
    fun getArchives(@Query("where") where: String): Observable<ApiBean.ApiListResponse<ScpModel>>

    // SCP故事版，带123查询
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModel")
    fun getStory(@Query("where") where: String, @Query("skip") skip: Int, @Query("limit") limit: Int): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModel")
    fun getTales(@Query("where") where: String): Observable<ApiBean.ApiListResponse<ScpModel>>

    // 故事系列，征文，设定中心
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModel")
    fun getLibraryItem(@Query("where") where: String): Observable<ApiBean.ApiListResponse<ScpModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/Config")
    fun getAppConfig(): Observable<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>


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