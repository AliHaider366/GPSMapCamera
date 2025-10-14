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
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import com.example.gpsmapcamera.models.StampCameraPosition
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.KEY_FOLDER_NAME
import com.example.gpsmapcamera.utils.PrefManager.getString
import com.example.gpsmapcamera.utils.animateRippleReveal
import com.example.gpsmapcamera.utils.showToast
import com.example.gpsmapcamera.utils.tooBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CameraManager(
    private val context: Context,
    val previewView: PreviewView,

    )
{
    private val appViewModel=(context.applicationContext as MyApp).appViewModel
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//    private var flashEnabled = false
    private var flashEnabled = 0
    private var aspectRatio: Int = AspectRatio.RATIO_4_3
    private var captureSoundEnabled = true
    private var isMirrorEnabled = false
    private var tapCapture=false
    var selectedQuality: ImageQuality = ImageQuality.HIGH // default
    private var activeRecording: Recording? = null
    private var currentZoom=0.0f
    /// QR code
    private var qrCodeAnalyzer: QRCodeAnalyzer? = null
    private var qrImageAnalysis: ImageAnalysis? = null
    private var isQRCodeEnabled = false
    private var isVideoRecordEnabled = false
    private var videoRecorder: VideoRecorder? = null

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

    fun setTapCapture(capture: Boolean) {
        tapCapture = capture
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

    fun toggleFlash(isFlashEnabled:Int) {

 /*       flashEnabled = isFlashEnabled
        if (imageCapture != null)
        {
//            imageCapture?.setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            imageCapture?.setFlashMode(when(flashEnabled){
                0-> ImageCapture.FLASH_MODE_OFF
                1-> ImageCapture.FLASH_MODE_ON
                2-> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            })

        }
        else startCamera()*/

        flashEnabled = isFlashEnabled

        if (imageCapture == null) {
            startCamera()
            return
        }

        val cameraControl = camera?.cameraControl

        when (flashEnabled) {
            0 -> { // OFF
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                cameraControl?.enableTorch(false)
            }

            1 -> { // ON (used for capture only)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                cameraControl?.enableTorch(false)
            }

            2 -> { // AUTO
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                cameraControl?.enableTorch(false)
            }

            3 -> { // TORCH (flash always ON)
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF // avoid conflict
                cameraControl?.enableTorch(true)
            }

            else -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                cameraControl?.enableTorch(false)
            }
        }

    }

    fun captureSound(soundEnabled:Boolean)
    {
        captureSoundEnabled=soundEnabled
    }

    fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
        
        // Stop custom video recorder if active
        videoRecorder?.stopRecording()
        videoRecorder = null

        camera?.cameraControl?.enableTorch(false)
    }

    fun startVideoRecording(
        onStarted: () -> Unit,
        onSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {

        val vc = videoCapture ?: return onError("VideoCapture not ready")

//        val saveFileName = appViewModel.saveFileName.removeSuffix(".jpg") + ".mp4"          //// replace
        val filename= appViewModel.fileNameFromPattern().removeSuffix(".jpg") + ".mp4"
        // Choose output options based on Android version
        val outputOptions: OutputOptions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //  Android 10 and above → MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, appViewModel.fileSavePath)
                }

                MediaStoreOutputOptions.Builder(
                    context.contentResolver,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                    .setContentValues(contentValues)
                    .build()
            } else {
                //  Android 9 and below → FileOutputOptions
//                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//                val appDir = File(moviesDir, BuildConfig.APPLICATION_ID)
//                if (!appDir.exists()) appDir.mkdirs()
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val subFolderName= getString(context,KEY_FOLDER_NAME)
                val appDir= if (subFolderName.isNotEmpty()) File(picturesDir,  "${BuildConfig.APPLICATION_ID}/$subFolderName")     ///saved to folder path
                else  File(picturesDir,  BuildConfig.APPLICATION_ID)

                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, filename)
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
            val filename= appViewModel.fileNameFromPattern()
            imageProxy.close()
            qrImageAnalysis?.clearAnalyzer() // stop after single frame
            bitmap?.let {
                saveCapturedBitmap(it, filename, format = ImageFormat.JPG){uri->           ////replace file name
                    showShutterEffect()
                    onCaptured(uri)
                }

            }
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

    fun startCamera(imageQuality:ImageQuality?=null,onStarted: (() -> Unit)?=null) {

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

            if (imageQuality != null) {
                selectedQuality=imageQuality
            }
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

                // --- Curtain Animation ---
                val container = previewView.parent as? FrameLayout
                container?.animateRippleReveal(previewView)

                if (onStarted != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        onStarted()
                    }, 500)
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhotoWithTimer(seconds: Int, countdownText: TextView, onSaved: (Uri?) -> Unit) {
        countdownText.visibility = View.VISIBLE

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
                    takePhoto(onSaved) // run immediately
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
//        val file = File(context.getExternalFilesDir(null), "${System.currentTimeMillis()}.jpg")
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        //  Play sound if enabled
        if (captureSoundEnabled) {
            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    val rotationDegrees = image.imageInfo.rotationDegrees

                    image.close()
                    showShutterEffect()
                    if (bitmap != null) {
                        val matrix = Matrix().apply {
                            // rotate correctly
                            postRotate(rotationDegrees.toFloat())

                            //  apply mirror (after rotation)
                            if (isMirrorEnabled) {
                                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                            }
                        }

                        val finalBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0,
                            bitmap.width, bitmap.height,
                            matrix, true
                        )

//                        val filename= appViewModel.saveFileName.updateFileNameWithCurrentValues(Date().formatForFile(),Date().getCurrentDay())
                        val filename= appViewModel.fileNameFromPattern()
                         saveCapturedBitmap(finalBitmap, filename, format = ImageFormat.JPG){uri->
                             onSaved(uri)
                        }

                    } else {
                        onSaved(null)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    context.showToast("Capture failed: ${exception.message}")
                    onSaved(null)
                }
            }
        )
    }

    // Convert ImageProxy → Bitmap
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun saveCapturedBitmap(bitmap: Bitmap,fileName: String ,format: ImageFormat,onCaptured: (Uri) -> Unit) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, appViewModel.fileSavePath)     ///saved to folder path
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                onCaptured(uri)
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val subFolderName= getString(context,KEY_FOLDER_NAME)
//            val file = if (subFolderName.isNotEmpty()) File(picturesDir,  "${BuildConfig.APPLICATION_ID}/$subFolderName")     ///saved to folder path
//            else  File(picturesDir,  BuildConfig.APPLICATION_ID)
            val folderDir = if (subFolderName.isNotEmpty()) File(picturesDir,  "${BuildConfig.APPLICATION_ID}/$subFolderName")     ///saved to folder path
            else  File(picturesDir,  BuildConfig.APPLICATION_ID)

            if (!folderDir.exists()) {
                folderDir.mkdirs()
            }

            val imageFile = File(folderDir, fileName)
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
             val uri = Uri.fromFile(imageFile)
             onCaptured(uri)
        }
    }

    /// for image filter
    fun getCorrectlyOrientedBitmap(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

//        val file = File(uri.path ?: return bitmap)
//        val exif = ExifInterface(file.absolutePath)
        // Read Exif metadata
        val exifInputStream = context.contentResolver.openInputStream(uri)
        val exif = ExifInterface(exifInputStream!!)
        exifInputStream.close()
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


    fun buildImageCapture(
        flashEnabled: Int,
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
//            .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .setFlashMode(when(flashEnabled){
                0-> ImageCapture.FLASH_MODE_OFF
                1-> ImageCapture.FLASH_MODE_ON
                2-> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            })
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

    fun setupTouchControls(onTap:(x: Float, y: Float)->Unit) {
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

                onTap(event.x, event.y) // pass tap coords

              /*  if (tapCapture)
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
                }*/

            }
            true
        }
    }


    fun takePhotoWithStamp(
        stampContainer: FrameLayout,
        stampPosition : StampCameraPosition = StampCameraPosition.TOP,
        onSaved: (Uri?) -> Unit
    ) {
        val imageCapture = imageCapture ?: return

        if (captureSoundEnabled) {
            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    val rotationDegrees = image.imageInfo.rotationDegrees
                    image.close()
                    showShutterEffect()

                    if (bitmap != null) {
                        val matrix = Matrix().apply {
                            postRotate(rotationDegrees.toFloat())
                            if (isMirrorEnabled) {
                                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                            }
                        }
                        val cameraBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0,
                            bitmap.width, bitmap.height,
                            matrix, true
                        )

                        // --- render stamp container ---
                        val stampBitmap = getViewBitmap(stampContainer)

                        // --- merge both bitmaps ---
                        val mergedBitmap = mergeBitmaps(cameraBitmap, stampBitmap,stampPosition)

                        // save
                        val filename = appViewModel.fileNameFromPattern()
                        saveCapturedBitmap(mergedBitmap, filename, format = ImageFormat.JPG) { uri ->
                            onSaved(uri)
                        }
                    } else {
                        onSaved(null)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    context.showToast("Capture failed: ${exception.message}")
                    onSaved(null)
                }
            }
        )
    }

    // Helper: render view into bitmap
    private fun getViewBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun mergeBitmaps(
        camera: Bitmap,
        overlay: Bitmap,
        position: StampCameraPosition = StampCameraPosition.TOP,
    ): Bitmap {
        val result = camera.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(result)

        // Convert dp to px
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            context.resources.displayMetrics
        ).toInt()

        // Scale overlay to match width of camera
        val scaledOverlay = Bitmap.createScaledBitmap(
            overlay,
            camera.width - (marginPx * 2), // subtract horizontal margins
            overlay.height * (camera.width - marginPx * 2) / overlay.width, // keep ratio
            true
        )

        val x = marginPx.toFloat()
        val y = when (position) {
            StampCameraPosition.TOP -> marginPx.toFloat()
            StampCameraPosition.BOTTOM -> (camera.height - scaledOverlay.height - marginPx).toFloat()
        }

        canvas.drawBitmap(scaledOverlay, x, y, null)
        return result
    }


    /*    private fun mergeBitmaps(
            camera: Bitmap,
            overlay: Bitmap,
            position: StampCameraPosition = StampCameraPosition.TOP // default
        ): Bitmap {
            val result = camera.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = android.graphics.Canvas(result)

            // Scale overlay to match width of camera
            val scaledOverlay = Bitmap.createScaledBitmap(
                overlay,
                camera.width,
                overlay.height * camera.width / overlay.width, // maintain aspect ratio
                true
            )

            val x = 0f
            val y = when (position) {
                StampCameraPosition.TOP -> 0f
                StampCameraPosition.BOTTOM -> (camera.height - scaledOverlay.height).toFloat()
            }

            canvas.drawBitmap(scaledOverlay, x, y, null)
            return result
        }*/

    fun takePhotoWithTimer(
        seconds: Int,
        countdownText: TextView,
        stampContainer: FrameLayout,
        stampPosition : StampCameraPosition = StampCameraPosition.TOP,
        onSaved: (Uri?) -> Unit
    ) {
        countdownText.visibility = View.VISIBLE
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

                    // 🚀 Capture with stamp
                    takePhotoWithStamp(stampContainer, stampPosition,onSaved)
                }
            }
        }
        handler.post(countdownRunnable)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startVideoRecordingWithStamp(
        stampContainer: FrameLayout,
        stampPosition: StampCameraPosition = StampCameraPosition.TOP,
        onStarted: () -> Unit,
        onSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val filename = appViewModel.fileNameFromPattern().removeSuffix(".jpg") + ".mp4"
            
            // Create output file
            val outputFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+, we'll use a temporary file and then move it
                File(context.cacheDir, filename)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val subFolderName = getString(context, KEY_FOLDER_NAME)
                val folderDir = if (subFolderName.isNotEmpty()) 
                    File(picturesDir, "${BuildConfig.APPLICATION_ID}/$subFolderName")
                else 
                    File(picturesDir, BuildConfig.APPLICATION_ID)
                
                if (!folderDir.exists()) folderDir.mkdirs()
                File(folderDir, filename)
            }

            val width = previewView.width.takeIf { it > 0 } ?: 1280
            val height = previewView.height.takeIf { it > 0 } ?: 720

            // Enable torch if flash is enabled (1 = ON, 2 = AUTO)
            if (flashEnabled == 1 || flashEnabled == 2) {
                camera?.cameraControl?.enableTorch(true)
            }

            videoRecorder = VideoRecorder(
                previewView = previewView,
                stampContainer = stampContainer,
                stampPosition = stampPosition,
                width = width,
                height = height,
                outputFile = outputFile,
                fileSavePath = appViewModel.fileSavePath,
                onSaved = onSaved,
                onError = onError
            )

            videoRecorder?.startRecording()
            onStarted()

        } catch (e: Exception) {
            onError("Video recording failed: ${e.message}")
        }
    }

    fun stopVideoRecordingWithStamp() {
        videoRecorder?.stopRecording()
        videoRecorder = null

//        camera?.cameraControl?.enableTorch(false)
    }

}