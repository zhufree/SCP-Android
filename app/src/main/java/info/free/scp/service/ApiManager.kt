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
    val portalApi: FreeApi = retrofit.create(FreeApi::class.java)
}