package info.zhufree.freelove67.service

import android.util.Log
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by zhufree on 2018/6/11.
 * Observer基类，处理网络错误
 */

abstract class BaseObserver<T> : Observer<T> {

    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: T) {
    }

    override fun onError(e: Throwable) {
        Log.i("observer", e.localizedMessage)
    }
}
