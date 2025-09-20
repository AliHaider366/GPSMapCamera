package com.example.gpsmapcamera

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gpsmapcamera.activities.PreviewImageActivity
import com.example.gpsmapcamera.cameraHelper.CameraManager
import com.example.gpsmapcamera.databinding.ActivityMainBinding
import com.example.gpsmapcamera.enums.ImageFormat
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var imageCapture: ImageCapture? = null
    private var flashEnabled = false
    private var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private lateinit var cameraManager: CameraManager

    private lateinit var gpuImage: GPUImage
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    var volumeButtonForCapture = true // Toggle this based on user preference

    val requestCode=1001
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun allPermissionsGranted(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissionsIfNeeded() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, requestCode)
        }
        else
        {
            cameraManager.startCamera()
            cameraManager.setupTouchControls{

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        gpuImage = GPUImage(this)
        // Set your desired filter
        val filter = GPUImageGrayscaleFilter() // or any other
        gpuImage.setFilter(filter)

        init()

    }

    private fun init()=binding.apply {

        cameraManager = CameraManager(this@MainActivity, previewView)

        requestPermissionsIfNeeded()
/*        if (allPermissionsGranted()) {
//            startCamera()
            cameraManager.startCamera()
            cameraManager.setupTouchControls()
        } else {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.CAMERA), 10)
        }*/

        cameraManager.setQRCodeDetectionEnabled(true) { result ->
            runOnUiThread {
                Toast.makeText(this@MainActivity, "QR Detected: $result", Toast.LENGTH_SHORT).show()
            }
        }

        // Brightness slider
        brightnessSeekBar.max = 10  // For -3 to +3
        brightnessSeekBar.progress = 5
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                cameraManager.setBrightness(progress - 5)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Aspect ratio spinner
        aspectRatioSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedRatio = when (pos) {
                    0 -> AspectRatio.RATIO_4_3
                    1 -> AspectRatio.RATIO_16_9
                    else -> AspectRatio.RATIO_4_3
                }
                cameraManager.setAspectRatio(selectedRatio)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Aspect ratio spinner
        gridSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val gridCount = when (pos) {
                    0 -> 0
                    1 -> 2
                    2 -> 3
                    3 -> 4
                    4 -> 5
                    else -> 3
                }
                gridOverlay.updateGrid(gridCount > 0, gridCount)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchCamera.setOnClickListener {
           /* cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()*/
            cameraManager.switchCamera()
        }

        captureBtn.setOnClickListener {
            cameraManager.takePhoto() {
                val intent = Intent(this@MainActivity, PreviewImageActivity::class.java)
                intent.putExtra("image_uri", it.toString())
                startActivity(intent)
                Toast.makeText(this@MainActivity, "Saved: $it", Toast.LENGTH_SHORT).show()
            }
        }

/*       zoomIn.setOnClickListener {
            camera?.cameraControl?.setZoomRatio((camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f) + 0.1f)
        }

        zoomOut.setOnClickListener {
            camera?.cameraControl?.setZoomRatio((camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f) - 0.1f)
        }*/

        flashToggle.setOnClickListener {
//            flashEnabled = !flashEnabled
//            imageCapture?.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            cameraManager.toggleFlash(true)

        }

      /*  previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event) // detect pinch

            if (event.pointerCount == 1 && event.action == MotionEvent.ACTION_DOWN) {
                val factory = previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point).build()
                camera?.cameraControl?.startFocusAndMetering(action)
            }

            true
        }

        scaleGestureDetector = ScaleGestureDetector(this@MainActivity, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        })
*/
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (volumeButtonForCapture) {
                    cameraManager.takePhoto { uri->
                        if (uri != null) {
                            Toast.makeText(this, "Saved: $uri", Toast.LENGTH_SHORT).show()
                            sendEmailWithImage(uri)
                        }
                    }
                } else {
                    cameraManager.zoomIn()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (volumeButtonForCapture) {
                    cameraManager.takePhoto {
                        Toast.makeText(this, "Saved: $it", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cameraManager.zoomOut()
                }
                return true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun shareImage(imageUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "Check out this photo I just captured! 📸")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Image")
        startActivity(chooser)
    }

    private fun sendEmailWithImage(imageUri: Uri) {

//        val emailIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "image/*"
//            putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@example.com")) // Optional
//            putExtra(Intent.EXTRA_SUBJECT, "Captured Photo")
//            putExtra(Intent.EXTRA_TEXT, "Here's a photo I just took using MyCameraApp.")
//            putExtra(Intent.EXTRA_STREAM, imageUri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        try {
//            startActivity(emailIntent)
//        }
//        catch (e: ActivityNotFoundException) {
//            Toast.makeText(this, "Gmail app is not installed", Toast.LENGTH_SHORT).show()
//        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*" // Use a more general MIME type for images
            putExtra(Intent.EXTRA_EMAIL, arrayOf("recipient@example.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Captured Image from MyCameraApp")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached image.")
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.google.android.gm") // Use setPackage() instead of `package`
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback: Remove setPackage to allow other apps or show a chooser
            intent.setPackage(null)
            val chooserIntent = Intent.createChooser(intent, "Send email via")
            try {
                startActivity(chooserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No email app is installed", Toast.LENGTH_SHORT).show()
            }
        }
      /*  // Check if Gmail can handle the intent
        val resolveInfo = packageManager.queryIntentActivities(intent, 0)
        if (resolveInfo.isNotEmpty()) {
            startActivity(intent)
        }
        else {
            // Fallback: Remove setPackage to allow other apps or show a chooser
            intent.setPackage(null)
            val chooserIntent = Intent.createChooser(intent, "Send email via")
            try {
                startActivity(chooserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No email app is installed", Toast.LENGTH_SHORT).show()
            }
        }*/
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(getExternalFilesDir(null), "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "Saved: ${photoFile.name}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startCamera()=binding.apply {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@MainActivity)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this@MainActivity, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this@MainActivity))
    }

    /*private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }*/

}

