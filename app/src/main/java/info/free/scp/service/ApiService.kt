package info.free.scp.service

import info.free.scp.PrivateConstants
import info.free.scp.bean.ApiBean
import info.free.scp.bean.FeedModel
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
    // --data-urlencode 'keys=score,playerName' \
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModelV4")
    fun getAllScp(@Query("skip") skip: Int,
                  @Query("limit") limit: Int,
                  @Query("keys") keys: String="scp_type,not_found,contest_link,contest_name,created_time," +
                          "desc,event_type,link,month,page_code,snippet,subtext,title,author,download_type,tags")
            : Observable<ApiBean.ApiListResponse<ScpModel>>

    // where "{\"download_type\":\"${download_type}\"}"
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModelV4")
    fun getPartDetail(@Query("skip") skip: Int,
                      @Query("limit") limit: Int,
                      @Query("where") where: String,
                      @Query("keys") keys: String="link,detail,download_type")
            : Observable<ApiBean.ApiListResponse<ScpModel>>


    // 获取正文
    // --data-urlencode 'limit=200' \
    // --data-urlencode 'skip=400' \
    // --data-urlencode 'keys=score,playerName' \
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/ScpModelV4")
    fun getScpDetail(@Query("skip") skip: Int,
                  @Query("limit") limit: Int,
                  @Query("keys") keys: String="detail")
            : Observable<ApiBean.ApiListResponse<ScpModel>>


    @GET("/latest_cn/{pageIndex}")
    fun getLatestCn(@Path("pageIndex") pageIndex: Int): Observable<ApiBean.ApiListResponse<FeedModel>>

    @GET("/latest_translated/{pageIndex}")
    fun getLatestTranslated(@Path("pageIndex") pageIndex: Int): Observable<ApiBean.ApiListResponse<FeedModel>>

    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/Config")
    fun getAppConfig(): Observable<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>
}