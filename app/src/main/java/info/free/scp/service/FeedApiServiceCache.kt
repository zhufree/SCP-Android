package info.free.scp.service

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class FeedApiServiceCache(
    private val client: OkHttpClient,
    private val getBaseUrl: () -> HttpUrl,
) {
    private var baseUrl: HttpUrl? = null
    private lateinit var service: ApiService

    @Synchronized
    fun get(): ApiService {
        val currentUrl = getBaseUrl()
        if (baseUrl != currentUrl) {
            service = Retrofit.Builder()
                .baseUrl(currentUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
            baseUrl = currentUrl
        }
        return service
    }
}
