package info.free.scp.service

import info.free.scp.SCPConstants
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.SCPConstants.LATEST_TRANSLATED
import info.free.scp.SCPConstants.TOP_RATED_ALL
import info.free.scp.SCPConstants.TOP_RATED_GOI
import info.free.scp.SCPConstants.TOP_RATED_SCP
import info.free.scp.SCPConstants.TOP_RATED_TALES
import info.free.scp.SCPConstants.TOP_RATED_WANDERS
import info.free.scp.bean.*
import info.free.scp.util.PreferenceUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Free on 2018/5/5.
 *
 */

class HttpManager {

    private val bmobRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCPConstants.BMOB_API_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    private val feedRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(PreferenceUtil.getApiUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val bmobApiService = bmobRetrofit.create(ApiService::class.java)
    private val feedApiService = feedRetrofit.create(ApiService::class.java)


    fun getAppConfig(handleConfig: (configList: List<ApiBean.ConfigResponse>) -> Unit) {
        bmobApiService.getAppConfig().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ApiBean.ConfigResponse>) {
                        handleConfig(t.results)
                    }
                })
    }

    suspend fun getLatest(feedType: Int = LATEST_CREATED, pageIndex: Int = 1): ApiBean.ApiListResponse<FeedModel> {
        return when (feedType) {
            LATEST_CREATED -> {
                feedApiService.getLatestCn(pageIndex)
            }
            LATEST_TRANSLATED -> {
                feedApiService.getLatestTranslated(pageIndex)
            }
            TOP_RATED_ALL -> {
                feedApiService.getTopRated(pageIndex)
            }
            TOP_RATED_SCP -> {
                feedApiService.getTopRatedScp(pageIndex)
            }
            TOP_RATED_TALES -> {
                feedApiService.getTopRatedTale(pageIndex)
            }
            TOP_RATED_GOI -> {
                feedApiService.getTopRatedGoi()
            }
            TOP_RATED_WANDERS -> {
                feedApiService.getTopRatedWander(pageIndex)
            }
            else -> {
                feedApiService.getLatestCn(pageIndex)
            }
        }
    }

    suspend fun getLatestIndex(): FeedIndexModel {
        return feedApiService.getLatestIndex()
    }

    suspend fun getDetail(link: String = "scp-001"): ApiBean.ApiListResponse<String> {
        return feedApiService.getDetail(link)
    }

    suspend fun getComment(link: String = "scp-013"): ApiBean.ApiListResponse<CommentModel> {
        val postStr = "{\"cookie\": \"${PreferenceUtil.getCookie()}\", \"agent\": \"${PreferenceUtil.getAgent()}\"}"
        val postBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), postStr)
        return feedApiService.getComment(link, postBody)
    }

    suspend fun getRandom(typeRange: String): ApiBean.ApiListResponse<ScpItemModel> {
        return feedApiService.getRandom(typeRange)
    }

    suspend fun getDirect(scpType: Int = SERIES, numberString: String = "-001")
            : ApiBean.ApiListResponse<ScpItemModel> {
        return feedApiService.getDirect(scpType, numberString)
    }


    companion object {
        val instance = HttpManager()
    }
}
