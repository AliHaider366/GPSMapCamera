package com.example.gpsmapcamera.cameraHelper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridOverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var gridSize = 3 // Default to 3x3
    var showGrid = true

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        style = Paint.Style.STROKE
        alpha = 80 // Semi-transparent
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!showGrid || gridSize < 2) return

        val cellWidth = width.toFloat() / gridSize
        val cellHeight = height.toFloat() / gridSize

        // Vertical lines
        for (i in 1 until gridSize) {
            val x = i * cellWidth
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }

        // Horizontal lines
        for (i in 1 until gridSize) {
            val y = i * cellHeight
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
        }
    }

    fun updateGrid(show: Boolean, size: Int) {
        showGrid = show
        gridSize = size
        invalidate()
    }
}
