package com.example.gpsmapcamera.cameraHelper

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
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
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.OutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.enums.ImageFormat
import com.example.gpsmapcamera.enums.ImageQuality
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.PrefManager.getCameraFlash
import com.example.gpsmapcamera.utils.animateLightSweep
import com.example.gpsmapcamera.utils.animateRippleReveal
import com.example.gpsmapcamera.utils.playCurtainAnimation
import com.example.gpsmapcamera.utils.tooBitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.hypot

class CameraManager(
    private val context: Context,
    private val previewView: PreviewView,

    )
{
    private val appViewModel=(context.applicationContext as MyApp).appViewModel
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
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
    private var isVideoRecordEnabled = false

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(context)
    }


    fun setVideoRecord(enabled: Boolean)
    {
        isVideoRecordEnabled = enabled
        val container = previewView.parent as? FrameLayout
//                container?.playCurtainAnimation(
//                    duration = 1000,
//                )
//                container?.animateLightSweep()
        startCamera()
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

    fun toggleFlash(isFlashEnabled:Boolean) {
        flashEnabled = isFlashEnabled
        if (imageCapture != null)
        {
            imageCapture?.setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
        }
        else startCamera()
    }

    fun captureSound(soundEnabled:Boolean)
    {
        captureSoundEnabled=soundEnabled
    }

    private var activeRecording: Recording? = null

    fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun startVideoRecording(
        onStarted: () -> Unit,
        onSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {

        val vc = videoCapture ?: return onError("VideoCapture not ready")

//        val saveFileName = generateImageFileName(prefix = "VID",extension = ".mp4")
        val saveFileName = appViewModel.saveFileName.removeSuffix(".jpg") + ".mp4"
       /* val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/${BuildConfig.APPLICATION_ID}")
        }

        val mediaStoreOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()*/
        // Choose output options based on Android version
        val outputOptions: OutputOptions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // ✅ Android 10 and above → MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, saveFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, SAVED_DEFAULT_FILE_PATH)
                }

                MediaStoreOutputOptions.Builder(
                    context.contentResolver,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                    .setContentValues(contentValues)
                    .build()
            } else {
                // ✅ Android 9 and below → FileOutputOptions
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val appDir = File(moviesDir, BuildConfig.APPLICATION_ID)
                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, saveFileName)
                FileOutputOptions.Builder(file).build()
            }

        val pendingRecording = when (outputOptions) {
            is MediaStoreOutputOptions -> vc.output.prepareRecording(context, outputOptions)
            is FileOutputOptions -> vc.output.prepareRecording(context, outputOptions)
            else -> {
                onError("Unsupported OutputOptions type")
                return
            }
        }

        val recording = if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            pendingRecording.withAudioEnabled()
        } else {
            pendingRecording
        }

        activeRecording = recording.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> onStarted()
                is VideoRecordEvent.Finalize -> {
                    if (!event.hasError()) {
                        onSaved(event.outputResults.outputUri)
                    } else {
                        onError("Recording error: ${event.error}")
                    }
                    activeRecording = null
                }
            }
        }
    }

    // --- Capture frame from Preview while recording ---
    fun capturePhotoFromPreviewView(onCaptured: (Uri) -> Unit) {
        qrImageAnalysis?.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            // Convert ImageProxy to Bitmap with proper rotation
            val bitmap = imageProxy.tooBitmap()
            imageProxy.close()
            qrImageAnalysis?.clearAnalyzer() // stop after single frame
            bitmap?.let {
                saveBitmap(context, it) { uri ->
                    showShutterEffect()
                    onCaptured(uri)
                }
            }
        }

    }
    fun saveBitmap(context: Context, bitmap: Bitmap, onCaptured: (Uri) -> Unit) {
        try {
            val name = appViewModel.saveFileName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val selectedFormat = ImageFormat.JPG

                // Android 10+ : save via MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, selectedFormat.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, SAVED_DEFAULT_FILE_PATH)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                    onCaptured(uri)
                }
            } else {
                // Android 9 and below : save to external files directory
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, BuildConfig.APPLICATION_ID)
                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, name)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                val uri = Uri.fromFile(file)
                onCaptured(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Flash effect like camera shutter
     */
    fun showShutterEffect() {
        val overlay = View(context).apply {
            setBackgroundColor(Color.WHITE)
            alpha = 0f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        (previewView.parent as? FrameLayout)?.addView(overlay)

        // Animate flash
        overlay.animate()
            .alpha(0.5f)
            .setDuration(50)
            .withEndAction {
                overlay.animate().alpha(0f).setDuration(100)
                    .withEndAction { (previewView.parent as? FrameLayout)?.removeView(overlay) }
                    .start()
            }
            .start()
    }

    fun startCamera() {

        if (isQRCodeEnabled && qrCodeAnalyzer != null) {
            qrImageAnalysis = ImageAnalysis.Builder()
//                .setTargetAspectRatio(aspectRatio)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context), qrCodeAnalyzer!!)
                }
        }
        else if(isVideoRecordEnabled)
        {
            qrImageAnalysis = buildImageAnalysis(aspectRatio)

        }

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()


            val preview = Preview.Builder()
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

            // Recorder with quality
            val qualitySelector = QualitySelector.from(
                Quality.HD,
                FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
            )
            val recorder = Recorder.Builder()
                .setAspectRatio(aspectRatio)
                .setQualitySelector(qualitySelector)
                .build()
            videoCapture = VideoCapture.withOutput(recorder)


            try {
                provider.unbindAll()
                camera= if (qrImageAnalysis != null && !isVideoRecordEnabled) provider.bindToLifecycle(     /// setup camera for QR
                    context as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    qrImageAnalysis,
                ) // add conditionally
                else if(qrImageAnalysis != null && isVideoRecordEnabled)       // setup camera for video
                {
                    provider.bindToLifecycle(
                        context as LifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture,
                        qrImageAnalysis
                    )
                }
                else provider.bindToLifecycle(          // setup camera for images
                    context as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                    )

                // --- COOL Curtain Animation ---
                val container = previewView.parent as? FrameLayout
//                container?.playCurtainAnimation(
//                    duration = 1000,
//                )
//                container?.animateLightSweep()
                container?.animateRippleReveal(previewView)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun applyCircularReveal(container: FrameLayout) {
        val cx = container.width / 2
        val cy = container.height / 2
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        val anim = ViewAnimationUtils.createCircularReveal(
            container,
            cx,
            cy,
            0f,
            finalRadius
        )
        container.alpha = 1f
        anim.duration = 700
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
    }
    fun takePhotoWithTimer(seconds: Int, countdownText: TextView, onSaved: (Uri?) -> Unit) {
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
        if (isVideoRecordEnabled)
        {
            capturePhotoFromPreviewView{

            }
            return
        }

        val imageCapture = imageCapture ?: return
        val file = File(context.getExternalFilesDir(null), "${System.currentTimeMillis()}.jpg")
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        // ✅ Play sound if enabled
        if (captureSoundEnabled) {
            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        }
        val selectedFormat = ImageFormat.JPG
        val (outputOptions, outputUri) = getImageOutputOptions(context,appViewModel.saveFileName, format = selectedFormat)  /// use this to save image

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
//                    onSaved(Uri.fromFile(file))
//                    val outputUri=Uri.fromFile(file)
                    showShutterEffect()

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

    //// flip the captured image mirror feature
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


    fun buildImageCapture(
        flashEnabled: Boolean,
        quality: ImageQuality,
        aspectRatio: Int
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


    fun buildImageAnalysis(aspectRatio: Int = AspectRatio.RATIO_16_9): ImageAnalysis {
        // Create ResolutionSelector like ImageCapture
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(aspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO)
            )
            .build() // For ImageAnalysis, we usually don’t need a specific resolutionStrategy

        return ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
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

    fun getImageOutputOptions(context: Context,   fileName: String ,format: ImageFormat): Pair<ImageCapture.OutputFileOptions, Uri?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName.removeSuffix(".jpg")) // No extension here
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, SAVED_DEFAULT_FILE_PATH)     ///saved to folder path
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
            val appDir = File(picturesDir, BuildConfig.APPLICATION_ID)      ///saved to folder path
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
//        return "${prefix}_${countFormatted}_${timeStamp}_${suffix}"
    }


}