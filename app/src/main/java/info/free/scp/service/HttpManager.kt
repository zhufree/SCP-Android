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
//    // 发送时，update需要去掉bmobObject的属性
//    private val postExclusionStrategy = object : ExclusionStrategy {
//
//        override fun shouldSkipField(fa: FieldAttributes): Boolean {
//            return fa.name == "createdAt" || fa.name.startsWith("_")
//                ||fa.name == "objectId" || fa.name == "updatedAt"// 把bmobObject的保留属性去掉
//        }
//
//        override fun shouldSkipClass(clazz: Class<*>): Boolean {
//            return false
//        }
//    }
//    private val postGson = GsonBuilder().setExclusionStrategies(postExclusionStrategy).create()
//
//
    val contentType = MediaType.parse("application/json")!!
    val json = JSON
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCPConstants.BMOB_API_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(stringBased(contentType, json::parse, json::stringify))
            .build()
    private val apiService = retrofit.create(ApiService::class.java)
//
    fun getAllScpSeriesModel(skip:Int, limit: Int, updateView: (eventList: List<ScpModel>) -> Unit) {
        apiService.getAllScpSeriesModel(limit, skip).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ScpModel>>() {
                    override fun onNext(t: ApiBean.ApiListResponse<ScpModel>) {
                        updateView(t.results)
                    }
                })
    }
//
//    fun getAllPhotoModel(updateView: (photoList: List<PhotoModel>) -> Unit) {
//        apiService.getAllPhotoModel().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<PhotoModel>>() {
//                    override fun onNext(t: ApiBean.ApiListResponse<PhotoModel>) {
//                        updateView(t.results)
//                    }
//                })
//    }
//
//    fun getAllArticleModel(updateView: (eventList: List<ArticleModel>) -> Unit) {
//        apiService.getAllArticleModel().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ApiBean.ApiListResponse<ArticleModel>>() {
//                    override fun onNext(t: ApiBean.ApiListResponse<ArticleModel>) {
//                        updateView(t.results)
//                    }
//                })
//    }
//
//    fun getArticle(objectId: String, doLater: (article: ArticleModel)->Unit) {
//        apiService.getArticle(objectId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ArticleModel>() {
//                    override fun onNext(t: ArticleModel) {
//                        doLater(t)
//                    }
//                })
//    }
//
//    fun addEvent(event: EventModel, updateView: (event: EventModel) -> Unit) {
//        val eventStr = postGson.toJson(event)
//        val eventBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), eventStr)
//        apiService.addNewEvent(eventBody).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ApiBean.AddResponse>() {
//                    override fun onNext(t: ApiBean.AddResponse) {
//                        updateView(event)
//                    }
//                })
//    }
//
//    fun addPhoto(photo: PhotoModel, updateView: (photo: PhotoModel) -> Unit) {
//        val bodyStr = postGson.toJson(photo)
//        Log.i(TAG, bodyStr)
//        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyStr)
//        Log.i(TAG, body.toString())
//        apiService.addNewPhoto(body).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ApiBean.AddResponse>() {
//                    override fun onNext(t: ApiBean.AddResponse) {
//                        updateView(photo)
//                    }
//                })
//    }
//
//    fun addArticle(article: ArticleModel, updateView: (article: ArticleModel) -> Unit) {
//        val articleStr = postGson.toJson(article)
//        val articleBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), articleStr)
//        apiService.addNewArticle(articleBody).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ApiBean.AddResponse>() {
//                    override fun onNext(t: ApiBean.AddResponse) {
//                        article.objectId = t.objectId
//                        updateView(article)
//                    }
//                })
//    }
//
//    fun uploadPhoto(file: File,  updateView: (picUrl: String) -> Unit) {
//        val body = Utils.fileToByteBody(file)
//        apiService.uploadPhoto(file.name ?: "default.jpg", body).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : BaseObserver<ApiBean.UploadResponse>() {
//                    override fun onNext(t: ApiBean.UploadResponse) {
//                        updateView(t.url)
//                    }
//                })
//    }
//
//    fun updateModel(model: BmobObject, modelType: String, updateView:()-> Unit) {
//        val modelStr = postGson.toJson(model)
//        val modelBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), modelStr)
//        when (modelType) {
//            EVENT_TYPE-> {
//                Log.i(TAG, postGson.toJson(model))
//                apiService.updateEvent(model.objectId, modelBody).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(object : BaseObserver<ApiBean.UpdateResponse>() {
//                            override fun onNext(t: ApiBean.UpdateResponse) {
//                                updateView()
//                            }
//                        })
//            }
//            PHOTO_TYPE-> {
//                apiService.updatePhoto(model.objectId, modelBody).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(object : BaseObserver<ApiBean.UpdateResponse>() {
//                            override fun onNext(t: ApiBean.UpdateResponse) {
//                                updateView()
//                            }
//                        })
//            }
//            ARTICLE_TYPE-> {
//                apiService.updateArticle(model.objectId, modelBody).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(object : BaseObserver<ApiBean.UpdateResponse>() {
//                            override fun onNext(t: ApiBean.UpdateResponse) {
//                                updateView()
//                            }
//                        })
//            }
//        }
//    }
//
//    fun deleteModel(objectId: String,modelType: String, updateView:() -> Unit) {
//        when (modelType) {
//            EVENT_TYPE-> {
//                apiService.deleteEvent(objectId).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnError {
//                            Log.i(TAG, it.localizedMessage)
//                        }
//                        .subscribe(object : BaseObserver<ApiBean.DeleteResponse>() {
//                            override fun onNext(t: ApiBean.DeleteResponse) {
//                                updateView()
//                            }
//                        })
//            }
//            PHOTO_TYPE-> {
//                apiService.deletePhoto(objectId).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(object : BaseObserver<ApiBean.DeleteResponse>() {
//                            override fun onNext(t: ApiBean.DeleteResponse) {
//                                updateView()
//                            }
//                        })
//            }
//            ARTICLE_TYPE-> {
//                apiService.deleteArticle(objectId).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(object : BaseObserver<ApiBean.DeleteResponse>() {
//                            override fun onNext(t: ApiBean.DeleteResponse) {
//                                updateView()
//                            }
//                        })
//            }
//        }
//    }

    companion object {
        val instance = HttpManager()

    }
}
