package info.free.scp.service

import android.util.Log
import info.free.scp.SCPConstants.Category.SERIES
import info.free.scp.SCPConstants.LATEST_CREATED
import info.free.scp.SCPConstants.LATEST_TRANSLATED
import info.free.scp.SCPConstants.TOP_RATED_ALL
import info.free.scp.SCPConstants.TOP_RATED_GOI
import info.free.scp.SCPConstants.TOP_RATED_SCP
import info.free.scp.SCPConstants.TOP_RATED_TALES
import info.free.scp.SCPConstants.TOP_RATED_WANDERS
import info.free.scp.ScpApplication
import info.free.scp.bean.*
import info.free.scp.util.PreferenceUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Free on 2018/5/5.
 *
 */

class HttpManager {

    private val httpClientBuilder = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val url = chain.request().url.toUrl().toString()
            if (ScpApplication.isDebug) {
                Log.i("api", url)
            }
            chain.proceed(chain.request())
        }

    private val feedRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(PreferenceUtil.getApiUrl())
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClientBuilder.build())
        .build()

    private val feedApiService = feedRetrofit.create(ApiService::class.java)
    private val htmlParserService = HtmlParserService()


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

    suspend fun getTag(link: String = "scp-001"): ApiBean.ApiListResponse<String> {
        return feedApiService.getTag(link)
    }

    suspend fun getScpByTag(tag: String): ApiBean.ApiListResponse<ScpItemModel> {
        return feedApiService.getScpByTag(tag)
    }

    suspend fun getAllTags(): ApiBean.ApiListResponse<TagModel> {
        return feedApiService.getAllTags()
    }

    suspend fun getComment(link: String = "scp-013"): ApiBean.ApiListResponse<CommentModel> {
//        val postStr =
//            "{\"cookie\": \"${PreferenceUtil.getCookie()}\", \"agent\": \"${PreferenceUtil.getAgent()}\"}"
//        val postBody = postStr.toRequestBody("application/json; charset=utf-8".toMediaType())
//        return feedApiService.getComment(link, postBody)
        return htmlParserService.getComment(link, PreferenceUtil.getCookie(), PreferenceUtil.getAgent())
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
