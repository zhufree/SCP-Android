package info.free.scp.util

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import info.free.scp.ScpApplication

/**
 * Created by zhufree on 2018/2/26.
 * toast 工具类
 */

object Toaster {

    var toast: Toast? = null

    fun show(message: String, len: Int = LENGTH_SHORT, context: Context? = ScpApplication.context) {
        toast = Toast.makeText(context, message, len)
        toast?.show()
    }

    fun showLong(message: String, len: Int = LENGTH_LONG, context: Context? = ScpApplication.context) {
        if (toast == null) {
            toast = Toast.makeText(context, message, len)
        } else {
            toast?.duration = LENGTH_LONG
        }
        toast?.show()
    }

    fun showLongX(message: String, context: Context? = ScpApplication.context) {
        showLong(message, context = context)
        showLong(message, context = context)
    }

    /**
     * 取消显示
     */
    fun end() {
        toast?.cancel()
    }
}
