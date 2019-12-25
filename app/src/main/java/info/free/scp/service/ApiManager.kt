package info.free.scp.service

import android.util.Log
import info.free.scp.SCPConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiManager {

    // 定义一个拦截器
    private val httpClientBuilder = OkHttpClient.Builder().addInterceptor { chain ->
        // 拦截器拦截的初始请求
        Log.i("api", chain.request().url().url().toString())
        chain.proceed(chain.request()) // 请求继续
    }

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCPConstants.BMOB_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClientBuilder.build())
            .build()
    val portalApi: PortalApi = retrofit.create(PortalApi::class.java)
//    val pictureApi: PictureApi = retrofit.create(PictureApi::class.java)
//    val userApi: UserApi = retrofit.create(UserApi::class.java)
//    val commentApi: CommentApi = retrofit.create(CommentApi::class.java)
//    val collectionApi: CollectionApi = retrofit.create(CollectionApi::class.java)
//    val likeApi: LikeApi = retrofit.create(LikeApi::class.java)
//    val notifyApi: NotifyApi = retrofit.create(NotifyApi::class.java)
//    val draftApi: DraftApi = retrofit.create(DraftApi::class.java)
//    val configApi: ConfigApi = retrofit.create(ConfigApi::class.java)
}