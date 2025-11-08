package com.example.gpstest

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HSVColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnColorChangedListener {
        fun onColorChanged(color: Int, h: Float, s: Float, v: Float)
    }

    var listener: OnColorChangedListener? = null

    private var hue: Float = 200f
    private var saturation: Float = 0.6f
    private var value: Float = 0.9f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
        setShadowLayer(8f, 0f, 0f, 0x66000000)
    }

    private var gradientBitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val roundedPath = Path()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#E6FFFFFF")
        strokeWidth = 3f
    }
    private val cornerRadius = 24f

    fun setHue(h: Float) {
        hue = clamp(h, 0f, 360f)
        invalidateGradient()
        notifyChanged()
        invalidate()
    }

    fun setSV(s: Float, v: Float) {
        saturation = clamp(s, 0f, 1f)
        value = clamp(v, 0f, 1f)
        notifyChanged()
        invalidate()
    }

    fun getColor(): Int = Color.HSVToColor(floatArrayOf(hue, saturation, value))

    private fun clamp(x: Float, a: Float, b: Float) = max(a, min(b, x))

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateRoundedPath()
        invalidateGradient()
    }

    private fun invalidateGradient() {
        if (width == 0 || height == 0) return
        gradientBitmap?.recycle()
        gradientBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gradientBitmap!!)

        // Diagonal gradient: bottom-left (dark gray) -> top-right (pure hue)
        val hueColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
        val diagonalShader = LinearGradient(
            0f, height.toFloat(),              // start: bottom-left
            width.toFloat(), 0f,               // end: top-right
            intArrayOf(Color.DKGRAY, hueColor),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = diagonalShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun updateRoundedPath() {
        roundedPath.reset()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        roundedPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Clip to rounded rect then draw gradient
        val save = canvas.save()
        canvas.clipPath(roundedPath)
        gradientBitmap?.let { canvas.drawBitmap(it, 0f, 0f, bitmapPaint) }

        val cx = saturation * width
        val cy = (1f - value) * height
        canvas.drawCircle(cx, cy, 20f, thumbPaint)
        canvas.restoreToCount(save)
        // Border on top
        canvas.drawPath(roundedPath, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = clamp(event.x, 0f, width.toFloat())
                val y = clamp(event.y, 0f, height.toFloat())
                saturation = x / width.toFloat()
                value = 1f - (y / height.toFloat())
                notifyChanged()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun notifyChanged() {
        listener?.onColorChanged(getColor(), hue, saturation, value)
    }
}


