package info.free.scp.service

import android.util.Log
import android.widget.Toast
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import info.free.scp.ScpApplication

open class BaseService {
    private val postExclusionStrategy = object : ExclusionStrategy {

        override fun shouldSkipField(fa: FieldAttributes): Boolean {
            return fa.name == "createdAt" || fa.name.startsWith("_")
                    || fa.name == "objectId" || fa.name == "updatedAt"// 把bmobObject的保留属性去掉
                    || fa.name == "lastUpdateTime" // 去掉用于存储本地更新时间的字段
        }

        override fun shouldSkipClass(clazz: Class<*>): Boolean {
            return false
        }
    }

    val postGson: Gson = GsonBuilder().setExclusionStrategies(postExclusionStrategy).create()



    fun serviceLog(msg: String?) {
        if (ScpApplication.isDebug) {
            Log.i("scp-service", msg.orEmpty())
        }
    }

    fun serviceError(error: String?) {
        if (ScpApplication.isDebug) {
            Log.e("scp-service", error.orEmpty())
        }
    }

    fun toast(msg: String) {
        Toast.makeText(ScpApplication.context, msg, Toast.LENGTH_SHORT).show()
    }

}
