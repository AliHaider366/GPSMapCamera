package com.example.gpstest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HueSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnHueChangedListener {
        fun onHueChanged(hue: Float)
    }

    var listener: OnHueChangedListener? = null

    private var hue: Float = 0f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E6FFFFFF")
        strokeWidth = 3f
    }
    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 5f
    }
    private val thumbFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3B82F6") // blue fill like screenshot
    }

    private val roundedPath = Path()
    private val cornerRadius = 35f

    fun setHue(value: Float) {
        hue = clamp(value, 0f, 360f)
        invalidate()
    }

    fun getHue(): Float = hue

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateRoundedPath()
    }

    private fun updateRoundedPath() {
        roundedPath.reset()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        roundedPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Track gradient: rainbow 0..360
        val colors = intArrayOf(
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA,
            Color.RED
        )
        trackPaint.shader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            colors, null, Shader.TileMode.CLAMP
        )

        val save = canvas.save()
        canvas.clipPath(roundedPath)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), trackPaint)
        canvas.restoreToCount(save)
        canvas.drawPath(roundedPath, borderPaint)

        // Thumb
        val cx = (hue / 360f) * width
        val cy = height / 2f
        canvas.drawCircle(cx, cy, 14f, thumbFillPaint)
        canvas.drawCircle(cx, cy, 16f, thumbStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = clamp(event.x, 0f, width.toFloat())
                hue = (x / width.toFloat()) * 360f
                listener?.onHueChanged(hue)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun clamp(x: Float, a: Float, b: Float): Float = max(a, min(b, x))
}


