package info.free.scp.view.detail

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar

class VerticalSeekBar : AppCompatSeekBar {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onDraw(canvas: Canvas?) {
        canvas?.rotate(-90f)
        canvas?.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                var i = 0
                i = max - (max * event.y / height) as Int
                progress = i
                Log.i("Progress", progress.toString() + "")
                onSizeChanged(width, height, 0, 0)
            }

            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }
}