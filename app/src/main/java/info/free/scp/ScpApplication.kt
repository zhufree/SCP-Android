package info.free.scp

import android.app.Application
import android.content.Context

/**
 * Created by zhufree on 2018/8/27.
 */

class ScpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
    companion object {
        var context: Context? = null
    }
}