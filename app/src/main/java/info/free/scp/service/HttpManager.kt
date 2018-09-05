package info.free.scp.service

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
            .addConverterFactory(stringBased(contentType, json::parse, json::stringify))
            .build()
    private val apiService = retrofit.create(ApiService::class.java)
//
    fun getScpSeriesModel(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getScpSeriesModel(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }

    fun getSeriesCnModel(skip:Int, limit: Int, updateView: (photoList: List<ScpModel>) -> Unit) {
        apiService.getSeriesCnModel(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }

    fun getJokeCnModel(updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getJokeCnModel().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }

    fun getJokeScp(updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getJokeModel().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getExScp(updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getExScp().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getArchivedScp(updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getArchivedScp().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getRemovedScp(updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getRemovedScp().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getDecommissionedScp(updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getDecommissionedScp().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getFirstStory(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getFirstStory(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getSecondStory(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getSecondStory(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
    fun getThirdStory(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getThirdStory(skip, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
