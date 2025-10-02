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
    var isPhiGrid = false // Flag to toggle Phi grid

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        style = Paint.Style.STROKE
        alpha = 80 // Semi-transparent
    }

    // Golden Ratio values
    private val goldenRatioPoints = floatArrayOf(0.382f, 0.618f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!showGrid || gridSize < 2) return

        val width = this.width.toFloat()
        val height = this.height.toFloat()


        if (isPhiGrid) {
            // Use loop instead of duplicate code
            goldenRatioPoints.forEach { ratio ->
                val x = width * ratio
                val y = height * ratio
                canvas.drawLine(x, 0f, x, height, paint)   // vertical
                canvas.drawLine(0f, y, width, y, paint)   // horizontal
            }

        } else {
            // Standard grid (3x3, 4x4, etc.)
            val cellWidth = width / gridSize
            val cellHeight = height / gridSize

            // Vertical lines
            for (i in 1 until gridSize) {
                val x = i * cellWidth
                canvas.drawLine(x, 0f, x, height, paint)
            }

            // Horizontal lines
            for (i in 1 until gridSize) {
                val y = i * cellHeight
                canvas.drawLine(0f, y, width, y, paint)
            }
        }
    }



    // Method to toggle grid type and size
    fun updateGrid(show: Boolean, size: Int, phiGrid: Boolean = false) {
        showGrid = show
        gridSize = size
        isPhiGrid = phiGrid
        invalidate()
    }
}