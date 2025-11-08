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

class ColorSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Mode { HUE, SATURATION }

    interface OnValueChangedListener {
        fun onValueChanged(value: Float)
    }

    var listener: OnValueChangedListener? = null

    private var mode: Mode = Mode.HUE
    private var hueForSaturation: Float = 0f
    private var value: Float = 0f // HUE: 0..360, SATURATION: 0..1

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
        color = Color.parseColor("#3B82F6")
    }

    private val roundedPath = Path()
    private var cornerRadius: Float = 0f
    private var thumbRadius: Float = 0f

    fun setMode(m: Mode) {
        mode = m
        invalidate()
    }

    fun setHue(h: Float) {
        hueForSaturation = clamp(h, 0f, 360f)
        if (mode == Mode.SATURATION) invalidate()
    }

    fun setValue(v: Float) {
        value = when (mode) {
            Mode.HUE -> clamp(v, 0f, 360f)
            Mode.SATURATION -> clamp(v, 0f, 1f)
        }
        invalidate()
    }

    fun getValue(): Float = value

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGeometry()
    }

    private fun updateGeometry() {
        cornerRadius = height / 2f
        thumbRadius = max(8f, height * 0.45f)
        roundedPath.reset()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        roundedPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Track gradient
        when (mode) {
            Mode.HUE -> {
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
            }
            Mode.SATURATION -> {
                val pureHue = Color.HSVToColor(floatArrayOf(hueForSaturation, 1f, 1f))
                trackPaint.shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(Color.DKGRAY, pureHue),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        }

        val save = canvas.save()
        canvas.clipPath(roundedPath)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), trackPaint)
        canvas.restoreToCount(save)
        canvas.drawPath(roundedPath, borderPaint)

        // Thumb position (keep inside track bounds)
        val t = when (mode) { Mode.HUE -> value / 360f; Mode.SATURATION -> value }
        val cx = lerp(thumbRadius, width - thumbRadius, clamp(t, 0f, 1f))
        val cy = height / 2f
        canvas.drawCircle(cx, cy, thumbRadius - 2f, thumbFillPaint)
        canvas.drawCircle(cx, cy, thumbRadius, thumbStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = clamp(event.x, thumbRadius, width - thumbRadius)
                val t = if (width - 2f * thumbRadius <= 0f) 0f else (x - thumbRadius) / (width - 2f * thumbRadius)
                value = when (mode) { Mode.HUE -> t * 360f; Mode.SATURATION -> t }
                listener?.onValueChanged(value)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
    private fun clamp(x: Float, a: Float, b: Float): Float = max(a, min(b, x))
}


