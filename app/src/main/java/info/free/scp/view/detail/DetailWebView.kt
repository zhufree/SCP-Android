package info.free.scp.view.detail

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.webkit.WebView
import kotlin.math.abs

/**
 * Created by zhufree on 2018/10/26.
 * 自定义webView
 */

class DetailWebView : WebView {
    private var mContext: Context? = null
    var computeVerticalScrollRange = -1
    var mListener: WebScrollListener? = null

    var hasTouchEnd = false
    var isMovingUp = false

    var lastX = 0f
    var lastY = 0f

    constructor(context: Context) : super(context) {
        onCreate(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        onCreate(context)
    }

    private fun onCreate(context: Context) {
        mContext = context
    }

    fun reset() {
        computeVerticalScrollRange = 0
        hasTouchEnd = false
        isMovingUp = false
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (computeVerticalScrollRange > 0) {
            if (oldt < t && computeVerticalScrollRange - t < 2000 && !hasTouchEnd) {
                // 向下滑且距离到了
                Log.i("detail", "已读完")
                hasTouchEnd = true
                isMovingUp = false
                mListener?.onScrollToBottom()
            } else if (oldt > t && computeVerticalScrollRange - t > 2000 && !isMovingUp) {
                // 向上滑且距离大于4000
                hasTouchEnd = false
                isMovingUp = true
                mListener?.onScrollUp()
            }
        } else {
            computeVerticalScrollRange = computeVerticalScrollRange()
        }
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            ACTION_DOWN -> {
                lastX = ev.x
                lastY = ev.y
            }
            ACTION_MOVE -> {
                Log.i("detail", "x = ${ev.x} , y = ${ev.y}")
                val dx = ev.x - lastX
                val dy = ev.y - lastY
                if (abs(dx) > abs(dy)) {
//                    animate().translationX(dx).setDuration(500).start()
//                    left +=  dx.toInt()
                } else if (hasTouchEnd) {
//                    animate().translationX(dy).setDuration(500).start()
//                    top += dy.toInt()
                }
            }
            ACTION_UP -> {
                val dx = ev.x - lastX
                val dy = ev.y - lastY
                if (abs(dx) > abs(dy)) {
//                    if (dx > 300) mListener?.toLastArticle() else mListener?.toNextArticle()
                } else {
//                    if (dy > 300) mListener?.toRandomArticle()
                }
//                animate().translationX(0f).setDuration(500).start()
//                animate().translationY(0f).setDuration(500).start()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    interface WebScrollListener {
        fun onScrollToBottom()
        fun onScrollUp()
        fun toNextArticle()
        fun toLastArticle()
        fun toRandomArticle()
    }
}
