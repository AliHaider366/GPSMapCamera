package com.example.gpsmapcamera.cameraHelper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import android.view.View

class LevelView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), SensorEventListener {

    private var rollAngle = 0f
    private var pitchAngle = 0f
    private var isLevelEnabled = true

    private val paintFixed = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val paintMoving = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var gravityValues = FloatArray(3)

    private val tolerance = 2f

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isLevelEnabled) {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isLevelEnabled) return

        val centerX = width / 2f
        val centerY = height / 2f
        val lineLength = width / 3f

        // detect flat by checking Z axis
        val isFlat = Math.abs(gravityValues[2]) > 8.5f   // near flat if Z ≈ 9.8

        if (isFlat) {
            //  Bubble level
            val radius = width *0.1f
            canvas.drawCircle(centerX, centerY, radius, paintFixed)

            // Fixed cross
            drawCross(canvas, centerX, centerY, paintFixed)

            // normalize bubble offset
            val gX = gravityValues[0] / 9.8f
            val gY = gravityValues[1] / 9.8f

            val offsetX = gX * radius
            val offsetY = gY * radius

            paintMoving.color =
                if (Math.abs(offsetX) < 5 && Math.abs(offsetY) < 5) Color.GREEN else Color.WHITE

            drawCross(canvas, centerX - offsetX, centerY + offsetY, paintMoving)
        } else {
            // Line level
            canvas.drawLine(
                centerX + lineLength * 0.7f, centerY,
                centerX + lineLength * 0.9f, centerY,
                paintFixed
            )
            canvas.drawLine(
                centerX - lineLength * 0.7f, centerY,
                centerX - lineLength * 0.9f, centerY,
                paintFixed
            )

            paintMoving.color = if (Math.abs(rollAngle) <= tolerance ||
                Math.abs(rollAngle) >=178) Color.GREEN else Color.WHITE

            Log.d("TAG", "rollangle ${Math.abs(rollAngle)}")
            canvas.save()
            canvas.rotate(-rollAngle, centerX, centerY)
            canvas.drawLine(
                centerX - lineLength * 0.7f, centerY,
                centerX + lineLength * 0.7f, centerY,
                paintMoving
            )
            canvas.restore()
        }
    }

    private fun drawCross(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        val size = 20f
        canvas.drawLine(x - size, y, x + size, y, paint)
        canvas.drawLine(x, y - size, x, y + size, paint)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isLevelEnabled) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix
                )
                SensorManager.getOrientation(remappedMatrix, orientationAngles)
                rollAngle = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                pitchAngle = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            }
            Sensor.TYPE_GRAVITY -> {
                gravityValues = event.values.clone()
            }
        }
        invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun setLevelEnabled(enabled: Boolean) {
        isLevelEnabled = enabled
        if (enabled) {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            sensorManager.unregisterListener(this)
        }
        invalidate()
    }
}
