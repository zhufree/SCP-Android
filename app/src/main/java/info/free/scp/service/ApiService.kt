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
    fun getLatestCn(@Path("pageIndex") pageIndex: Int): Observable<ApiBean.ApiListResponse<FeedModel>>

    @GET("/latest_translated/{pageIndex}")
    fun getLatestTranslated(@Path("pageIndex") pageIndex: Int): Observable<ApiBean.ApiListResponse<FeedModel>>

    // TODO 把key也放进常量里
    @Headers(
            "Content-Type:application/json",
            "X-Bmob-Application-Id:${PrivateConstants.APP_ID}",
            "X-Bmob-REST-API-Key:${PrivateConstants.API_KEY}"
    )
    @GET("1/classes/Config")
    fun getAppConfig(): Observable<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>
}