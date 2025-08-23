package com.example.gpsmapcamera.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import kotlin.math.roundToInt

class VerticalSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.seekBarStyle
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // swap width & height when size changes
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // swap measure specs so width/height are flipped
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        // rotate canvas so SeekBar is vertical
        canvas.rotate(-90f)
        canvas.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                // calculate progress based on touch y
                val newProgress = (max - (max * event.y / height)).roundToInt()
                    .coerceIn(0, max)
                progress = newProgress
                onSizeChanged(width, height, 0, 0)
            }
        }
        return true
    }
}
