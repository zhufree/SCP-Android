package info.free.scp.service

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import info.free.scp.ScpApplication
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

open class BaseService : AnkoLogger {
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

    suspend fun <T : Any?> apiCall(call: suspend () -> T): T? {
        try {
            return call.invoke()

        } catch (e: Exception) {
        }
        return null
    }


    fun serviceLog(msg: String?) {
        if (ScpApplication.isDebug) {
            info { msg }
        }
    }

    fun serviceError(error: String?) {
        if (ScpApplication.isDebug) {
            error { error }
        }
    }

    fun toast(msg: String) {
        ScpApplication.context.toast(msg)
    }

}