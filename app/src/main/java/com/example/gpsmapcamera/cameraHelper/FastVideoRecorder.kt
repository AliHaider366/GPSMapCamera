package com.example.gpsmapcamera.cameraHelper

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresPermission
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.models.StampCameraPosition
import com.example.gpsmapcamera.utils.PrefManager.KEY_FOLDER_NAME
import com.example.gpsmapcamera.utils.PrefManager.getString
import java.io.File

class FastVideoRecorder(
    private val previewView: PreviewView,
    private val stampContainer: ConstraintLayout,
    private val textContainer: FrameLayout,
    private val stampPosition: StampCameraPosition,
    private val width: Int,
    private val height: Int,
    private val bitRate: Int = 4_000_000,
    private val frameRate: Int = 30,
    private val outputFile: File,
    private val fileSavePath: String,
    private val onSaved: (Uri) -> Unit,
    private val onError: (String) -> Unit
) {

    private var drawingHandler: Handler? = null
    private var drawingRunnable: Runnable? = null
    private var stampBitmap: Bitmap? = null
    private var textBitmap: Bitmap? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (isRecording) return

        // Initialize the encoder and start in background
        encoder = VideoEncoder(width, height, bitRate, frameRate, outputFile)
        encoder?.startRecording()

        // Initialize stamp bitmap once
        initializeStampBitmap()

        // Start drawing loop on UI thread
        drawingHandler = Handler(Looper.getMainLooper())
        startDrawingLoop()

        isRecording = true
        Log.d(TAG, "Video recording started")
    }

    private fun initializeStampBitmap() {
        // Scale stamp once
        stampBitmap = getViewBitmap(stampContainer)?.let { original ->
            Bitmap.createScaledBitmap(
                original,
                width,
                original.height * width / original.width,
                true
            )
        }
        textBitmap = getViewBitmap(textContainer)?.let { original ->
            Log.e(TAG, "initializeStampBitmap")

            Bitmap.createScaledBitmap(
                original,
                width,
                original.height * width / original.width,
                true
            )

        }
    }

    private fun startDrawingLoop() {
        val inputSurface = encoder?.inputSurface ?: return
        val frameInterval = (1000L / frameRate) // Calculate frame interval

        drawingRunnable = object : Runnable {
            override fun run() {
                if (!isRecording) return

                try {
                    val canvas = inputSurface.lockCanvas(null)

                    // Draw camera preview
                    val previewBitmap = getPreviewBitmap()
                    if (previewBitmap != null) {
                        canvas.drawBitmap(previewBitmap, 0f, 0f, null)
                    }

                    // Draw stamp overlay (reuse pre-scaled bitmap)
                    textBitmap?.let {
                        val y = (height - it.height).toFloat() // bottom
                        canvas.drawBitmap(it, 0f, y, null)
                    }

                    stampBitmap?.let {
                        val y = when (stampPosition) {
                            StampCameraPosition.TOP -> 0f
                            StampCameraPosition.BOTTOM -> (height - it.height).toFloat()
                        }
                        canvas.drawBitmap(it, 0f, y, null)
                    }

                    inputSurface.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    Log.e(TAG, "Drawing loop error", e)
                }

                if (isRecording) {
                    drawingHandler?.postDelayed(this, frameInterval)
                }
            }
        }

        drawingHandler?.post(drawingRunnable!!)
    }


    private var encoder: VideoEncoder? = null
    private var drawThread: HandlerThread? = null
    private var drawHandler: Handler? = null
    private var isRecording = false
    private var overlayBitmap: Bitmap? = null



    private val drawLoop = object : Runnable {
        override fun run() {
            if (!isRecording) return
            val surface = encoder?.inputSurface ?: return

            try {
                val canvas = surface.lockCanvas(null)

                // Use an efficient way to get the preview frame (replace this with ImageReader or SurfaceTexture)
                previewView.bitmap?.let { bmp ->
                    canvas.drawBitmap(bmp, 0f, 0f, null)
                }

                overlayBitmap?.let { overlay ->
                    val y = if (stampPosition == StampCameraPosition.BOTTOM)
                        (height - overlay.height).toFloat()
                    else 0f
                    canvas.drawBitmap(overlay, 0f, y, null)
                }

                surface.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                Log.e(TAG, "Frame draw error", e)
            }

            if (isRecording) drawHandler?.postDelayed(this, 1000L / frameRate)
        }
    }

    fun stopRecording() {
        isRecording = false
        drawHandler?.removeCallbacksAndMessages(null)
        drawThread?.quitSafely()
        drawThread = null
        drawHandler = null
        encoder?.stopRecording()
        saveVideoFile()
    }



    private fun saveVideoFile() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+, save to MediaStore
                val filename = outputFile.name
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, fileSavePath)
                }

                val resolver = previewView.context.contentResolver
                val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    // Copy the file to the MediaStore location
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    outputFile.delete() // Clean up temporary file
                    onSaved(uri)
                    Log.d(TAG, "Video saved to MediaStore: $uri")
                } else {
                    onError("Failed to save video to MediaStore")
                }
            } else {
                // For older Android versions, return file URI
                val uri = Uri.fromFile(outputFile)
                onSaved(uri)
                Log.d(TAG, "Video saved to file: $uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video file", e)
            onError("Failed to save video: ${e.message}")
        }
    }



    private fun getPreviewBitmap(): Bitmap? {
        return try {
            previewView.bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error getting preview bitmap", e)
            null
        }
    }


    private fun getViewBitmap(view: View): Bitmap? {
        return try {
            if (view.width <= 0 || view.height <= 0) return null

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            Log.e(TAG, "getViewBitmap")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view bitmap", e)
            null
        }
    }

    fun isRecording(): Boolean = isRecording

    companion object {
        private const val TAG = "VideoRecorder"
    }
}