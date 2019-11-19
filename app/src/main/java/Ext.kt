import android.util.Log
import info.free.scp.ScpApplication
import info.free.scp.bean.ApiBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

/**
 *
 * Created by zhufree on 2019-07-16.
 */

suspend fun <T : ApiBean.ApiResponse> executeResponse(response: T,
                                                      errorBlock: () -> Unit,
                                                      successBlock: (response: ApiBean.ApiResponse) -> Unit) {
    withContext(Dispatchers.Main) {
        if (response.error.isNullOrEmpty()) {
            successBlock(response)
        } else {
            errorBlock()
        }
    }
}

suspend fun <T : Any> apiCall(call: suspend () -> T): T? {
    try {
        return call.invoke()

    } catch (e: Exception) {
        Log.i("network", e.toString())
        withContext(Dispatchers.Main) {
            toast("网络请求出错")
        }
    }
    return null
}

fun toast(msg: String) = ScpApplication.context.toast(msg)
fun info(msg: String) = ScpApplication.currentActivity?.info { msg }