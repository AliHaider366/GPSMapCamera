package com.example.gpsmapcamera.cameraHelper

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.camera.view.PreviewView
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.models.StampCameraPosition
import com.example.gpsmapcamera.utils.PrefManager.KEY_FOLDER_NAME
import com.example.gpsmapcamera.utils.PrefManager.getString
import java.io.File
import java.io.InputStream

class VideoRecorder(
    private val previewView: PreviewView,
    private val stampContainer: FrameLayout,
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
    private var encoder: VideoEncoder? = null
    private var drawingHandler: Handler? = null
    private var drawingRunnable: Runnable? = null
    private var isRecording = false

    fun startRecording() {
        if (isRecording) return

        encoder = VideoEncoder(width, height, bitRate, frameRate, outputFile)
        encoder?.startRecording()

        drawingHandler = Handler(Looper.getMainLooper())
        startDrawingLoop()

        isRecording = true
        Log.d(TAG, "Video recording started")
    }

    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        drawingHandler?.removeCallbacks(drawingRunnable!!)
        drawingHandler = null
        drawingRunnable = null

        encoder?.stopRecording()
        
        // Save the video file after recording stops
        saveVideoFile()
        
        encoder = null

        Log.d(TAG, "Video recording stopped")
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

                    // Draw stamp overlay
                    drawStampOverlay(canvas)

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

    private fun getPreviewBitmap(): Bitmap? {
        return try {
            previewView.bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error getting preview bitmap", e)
            null
        }
    }

    private fun drawStampOverlay(canvas: Canvas) {
        try {
            // Create a bitmap from the stamp container
            val stampBitmap = getViewBitmap(stampContainer)
            
            if (stampBitmap != null) {
                // Scale the stamp to fit the video width
                val scaledStamp = Bitmap.createScaledBitmap(
                    stampBitmap,
                    width,
                    stampBitmap.height * width / stampBitmap.width,
                    true
                )

                // Calculate position based on stamp position
                val y = when (stampPosition) {
                    StampCameraPosition.TOP -> 0f
                    StampCameraPosition.BOTTOM -> (height - scaledStamp.height).toFloat()
                }

                canvas.drawBitmap(scaledStamp, 0f, y, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing stamp overlay", e)
        }
    }

    private fun getViewBitmap(view: View): Bitmap? {
        return try {
            if (view.width <= 0 || view.height <= 0) return null
            
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
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
