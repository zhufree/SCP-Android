package info.free.scp.service

//import com.jakewharton.retrofit2.converter.kotlinx.serialization.serializationConverterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.stringBased
import info.free.scp.SCPConstants
import info.free.scp.bean.ApiBean
import info.free.scp.bean.ScpModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.JSON
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


/**
 * Created by Free on 2018/5/5.
 *
 */

class HttpManager {
    private val TAG = javaClass.name

    val contentType = MediaType.parse("application/json")!!
    val json = JSON
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCPConstants.BMOB_API_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .addConverterFactory(serializationConverterFactory(contentType, JSON))
            .addConverterFactory(stringBased(contentType, json::parse, json::stringify))
            .build()
    private val apiService = retrofit.create(ApiService::class.java)

    fun getAppConfig(handleConfig: (configList: List<ApiBean.ConfigResponse>) -> Unit) {
        apiService.getAppConfig().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ApiBean.ConfigResponse>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ApiBean.ConfigResponse>) {
                       handleConfig(t.results)
                    }
                })
    }
//
    fun getAllScp(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getAllScp(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }

                    override fun onFail() {
                        updateView(emptyList())
                    }
                })
    }

    fun getDetail(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getScpDetail(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }


    companion object {
        val instance = HttpManager()

    }
}
