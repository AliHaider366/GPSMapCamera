package com.example.gpsmapcamera.cameraHelper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.enums.ImageFormat
import com.example.gpsmapcamera.enums.ImageQuality
import com.example.gpsmapcamera.utils.PrefManager
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraManager(
    private val context: Context,
    private val previewView: PreviewView,

    )
{
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashEnabled = false
    private var aspectRatio: Int = AspectRatio.RATIO_4_3
    private var captureSoundEnabled = true
    private var isMirrorEnabled = false
    private val tapCapture=false
    var selectedQuality: ImageQuality = ImageQuality.HIGH // default

    private var currentZoom=0.0f
    /// QR code
    private var qrCodeAnalyzer: QRCodeAnalyzer? = null
    private var qrImageAnalysis: ImageAnalysis? = null
    private var isQRCodeEnabled = false

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(context)
    }


    fun setQRCodeDetectionEnabled(enabled: Boolean, onResult: (String) -> Unit) {
        isQRCodeEnabled = enabled
        if (enabled) {
            qrCodeAnalyzer = QRCodeAnalyzer(context, onResult)
        } else {
            qrCodeAnalyzer?.stop()
            qrCodeAnalyzer = null
            qrImageAnalysis = null
        }
        startCamera() // restart with updated config
    }

    fun setMirror(enabled: Boolean) {
        isMirrorEnabled = enabled
    }

    fun setAspectRatio(ratio: Int) {
        aspectRatio = ratio

        // Resize preview container based on aspect ratio
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val previewHeight = when (aspectRatio) {
            AspectRatio.RATIO_4_3 -> (screenWidth / 3f) * 4f
            AspectRatio.RATIO_16_9 -> (screenWidth / 9f) * 16f
            else -> (screenWidth / 3f) * 4f
        }

        val container = (previewView.parent as View)
        container.layoutParams = container.layoutParams.apply {
            height = previewHeight.toInt()
        }
        startCamera()
    }

    fun zoomIn() {
        camera?.cameraControl?.setLinearZoom((currentZoom + 0.1f).coerceAtMost(1.0f))
        currentZoom = (currentZoom + 0.1f).coerceAtMost(1.0f)
    }

    fun zoomOut() {
        camera?.cameraControl?.setLinearZoom((currentZoom - 0.1f).coerceAtLeast(0.0f))
        currentZoom = (currentZoom - 0.1f).coerceAtLeast(0.0f)
    }

    fun zoom1x2x(zoom:Float)
    {
        camera?.cameraControl?.setZoomRatio(zoom)
    }
    fun setBrightness(level: Int) {
        val range = camera?.cameraInfo?.exposureState?.exposureCompensationRange
        if (range != null && level in range) {
            camera?.cameraControl?.setExposureCompensationIndex(level)
        }
    }

    fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        startCamera()
    }

    fun toggleFlash() {
        flashEnabled = !flashEnabled
        imageCapture?.flashMode = if (flashEnabled)
            ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    fun takePhotoWithDelay(seconds: Int, countdownText: TextView, onSaved: (Uri?) -> Unit) {
        /*Toast.makeText(context, "Capturing in $seconds sec...", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            takePhoto(onSaved)
        }, seconds * 1000L)*/

        countdownText.visibility = View.VISIBLE

    /*    val duration = (seconds * 1000L) - 1000L // subtract 1 second to stop at 1
        val timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000) + 1
                countdownText.text = secondsLeft.toString()
            }

            override fun onFinish() {
                countdownText.text = ""
                countdownText.visibility = View.GONE
                takePhoto(onSaved)
            }
        }

        timer.start()*/

        var currentSecond = seconds
        val handler = Handler(Looper.getMainLooper())

        val countdownRunnable = object : Runnable {
            override fun run() {
                if (currentSecond > 0) {
                    countdownText.text = currentSecond.toString()
                    currentSecond--
                    handler.postDelayed(this, 1000)
                } else {
                    countdownText.visibility = View.GONE
                    countdownText.text = ""
                    takePhoto(onSaved) // run immediately after 1, don't show 0
                }
            }
        }

        handler.post(countdownRunnable)
    }

    fun takePhoto(onSaved: (Uri?) -> Unit) {
        val imageCapture = imageCapture ?: return
//        val file = File(context.getExternalFilesDir(null), "${System.currentTimeMillis()}.jpg")
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        // ✅ Play sound if enabled
        if (captureSoundEnabled) {
            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        }
        val selectedFormat = ImageFormat.JPG

        val (outputOptions, outputUri) = getImageOutputOptions(context, format = selectedFormat)

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
//                    onSaved(Uri.fromFile(file))
                    if (isMirrorEnabled) {
                        if (outputUri != null) {
                            mirrorImageFromUri(outputUri) { mirroredUri ->
                                onSaved(mirroredUri) // Use mirrored image
                            }
                        }
                    } else {
                        onSaved(outputUri) // Use original image
                    }
//                    val uri = Uri.fromFile(file)
//                    applyFilterToImage(uri) { filteredUri ->
//                        onSaved(filteredUri)
//                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun mirrorImageFromUri(uri: Uri, callback: (Uri) -> Unit) {
        val bitmap = getCorrectlyOrientedBitmap(uri)

        // Flip horizontally (mirror)
        val matrix = Matrix().apply {
            preScale(-1f, 1f)  // Horizontal mirror
        }

        val mirroredBitmap = Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )

        // Save mirrored bitmap to new file
        val mirroredFile = File(
            context.getExternalFilesDir(null),
            "mirrored_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(mirroredFile).use {
            mirroredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        callback(Uri.fromFile(mirroredFile))
    }

    fun getCorrectlyOrientedBitmap(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val file = File(uri.path ?: return bitmap)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun applyFilterToImage(uri: Uri, callback: (Uri) -> Unit) {
        // Load and correct orientation
        val rotatedBitmap = getCorrectlyOrientedBitmap(uri)        // Initialize GPUImage
        val gpuImage = GPUImage(context)
//        gpuImage.setFilter(GPUImageSketchFilter()) // Replace with your desired filter
        gpuImage.setFilter(GPUImageGrayscaleFilter()) // Replace with your desired filter
//        gpuImage.setFilter(GPUImageContrastFilter(2.0f)) // Replace with your desired filter
        gpuImage.setImage(rotatedBitmap)
        val filteredBitmap = gpuImage.bitmapWithFilterApplied

        val filteredFile = File(context.getExternalFilesDir(null), "filtered_${System.currentTimeMillis()}.jpg")
        FileOutputStream(filteredFile).use {
            filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        callback(Uri.fromFile(filteredFile))
    }

    fun startCamera() {

        if (isQRCodeEnabled && qrCodeAnalyzer != null) {
            qrImageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context), qrCodeAnalyzer!!)
                }
        }

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder()
//                .setTargetAspectRatio(aspectRatio)
                .build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

         /*   imageCapture = ImageCapture.Builder()
                .setTargetResolution(selectedQuality.resolution)
                .setFlashMode(if (flashEnabled)
                    ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                .setTargetAspectRatio(aspectRatio)
                .build()*/
            imageCapture=buildImageCapture(flashEnabled,selectedQuality,aspectRatio)

            try {
                provider.unbindAll()
               camera= if (qrImageAnalysis != null) provider.bindToLifecycle(
                        context as androidx.lifecycle.LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        qrImageAnalysis) // add conditionally

                else provider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun buildImageCapture(
        flashEnabled: Boolean,
        quality: ImageQuality,
        aspectRatio: Int // AspectRatio.RATIO_16_9, AspectRatio.RATIO_4_3, etc.
    ): ImageCapture {

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(aspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO)
            )
            .setResolutionStrategy(
                ResolutionStrategy(
                    quality.resolution,
                    quality.fallbackRule
                )
            )
            .build()

        return ImageCapture.Builder()
            .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .setResolutionSelector(resolutionSelector)
            .build()
    }

    fun setupTouchControls(onTap:()->Unit) {
        val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val zoom = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                camera?.cameraControl?.setZoomRatio(zoom * detector.scaleFactor)
                return true
            }
        })

        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)

            if (event.pointerCount == 1 && event.action == MotionEvent.ACTION_DOWN) {

                onTap()

                if (tapCapture)
                {
                    takePhoto {
                        Toast.makeText(context, "Saved: it", Toast.LENGTH_SHORT).show()
                }
                }
                else
                {
                    val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    camera?.cameraControl?.startFocusAndMetering(action)
                }

            }
            true
        }
    }

    fun getImageOutputOptions(context: Context,   fileName: String = generateImageFileName(),format: ImageFormat): Pair<ImageCapture.OutputFileOptions, Uri?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName.removeSuffix(".jpg")) // No extension here
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/${BuildConfig.APPLICATION_ID}")
            }

            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val resolver = context.contentResolver
            val uri = resolver.insert(contentUri, contentValues)

            if (uri == null) {
                throw IOException("Failed to create MediaStore entry")
            }

            val outputStream = resolver.openOutputStream(uri)
                ?: throw IOException("Failed to open output stream for URI")

            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputStream).build()
            Pair(outputOptions, uri)

        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "MyCameraApp")
            if (!appDir.exists()) appDir.mkdirs()

            val file = File(appDir, fileName)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            Pair(outputOptions, Uri.fromFile(file))
        }
    }

    fun generateImageFileName(
        prefix: String = "IMG",
        suffix: String = "",
        includeTimestamp: Boolean = true,
        extension: String = ".jpg"
    ): String {

        val count = PrefManager.getImageCount(context,0) + 1
        PrefManager.saveImageCount(context,count)

        // Format the count with leading zeros: 001, 002, ...
        val countFormatted = String.format("%03d", count)


        val timeStamp = if (includeTimestamp) {
//            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            SimpleDateFormat(
                "yyyy-MM-dd_EEEE_HH-mm-ss",  // Full date + day + 24-hour time
                Locale.getDefault()
            ).format(Date())
        } else {
            ""
        }

        return "${prefix}_${countFormatted}_${timeStamp}_${suffix}_$extension"
    }


}