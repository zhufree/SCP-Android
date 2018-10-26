package info.free.scp.view.detail

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView

/**
 * Created by zhufree on 2018/10/26.
 * 自定义webView
 */

class DetailWebView : WebView {
    private var mContext: Context? = null
    private var computeVerticalScrollRange = -1
    var mListener: WebScrollListener? = null

    var hasTouchEnd = false

    constructor(context: Context) : super(context) {
        onCreate(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        onCreate(context)
    }

    private fun onCreate(context: Context) {
        mContext = context
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (computeVerticalScrollRange > 0) {
            if (computeVerticalScrollRange - t < 2000 && !hasTouchEnd) {
                Log.i("detail", "已读完")
                hasTouchEnd = true
                mListener?.onScrollToBottom()
            }
        } else {
            computeVerticalScrollRange = computeVerticalScrollRange()
        }
        super.onScrollChanged(l, t, oldl, oldt)
    }

    interface WebScrollListener {
        fun onScrollToBottom()
    }
}
