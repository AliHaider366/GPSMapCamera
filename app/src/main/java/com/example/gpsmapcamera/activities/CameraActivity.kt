package com.example.gpsmapcamera.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.gpsmapcamera.cameraHelper.CameraManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.ActivityCameraBinding
import com.example.gpsmapcamera.utils.PrefManager.getAutoFocus
import com.example.gpsmapcamera.utils.PrefManager.getCameraFlash
import com.example.gpsmapcamera.utils.PrefManager.getCameraGrid
import com.example.gpsmapcamera.utils.PrefManager.getCameraLevel
import com.example.gpsmapcamera.utils.PrefManager.getCameraMirror
import com.example.gpsmapcamera.utils.PrefManager.getCameraRatio
import com.example.gpsmapcamera.utils.PrefManager.getCameraTimer
import com.example.gpsmapcamera.utils.PrefManager.getCaptureSound
import com.example.gpsmapcamera.utils.PrefManager.getWhiteBalance
import com.example.gpsmapcamera.utils.PrefManager.saveAutoFocus
import com.example.gpsmapcamera.utils.PrefManager.saveCameraFlash
import com.example.gpsmapcamera.utils.PrefManager.saveCameraGrid
import com.example.gpsmapcamera.utils.PrefManager.saveCameraLevel
import com.example.gpsmapcamera.utils.PrefManager.saveCameraMirror
import com.example.gpsmapcamera.utils.PrefManager.saveCameraRatio
import com.example.gpsmapcamera.utils.PrefManager.saveCameraTimer
import com.example.gpsmapcamera.utils.PrefManager.saveCaptureSound
import com.example.gpsmapcamera.utils.PrefManager.saveWhiteBalance
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.hideSystemBars
import com.example.gpsmapcamera.utils.isPermissionGranted
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.openAppSettings
import com.example.gpsmapcamera.utils.registerPermissionLauncher
import com.example.gpsmapcamera.utils.requestPermission
import com.example.gpsmapcamera.utils.setCompoundDrawableTintAndTextColor
import com.example.gpsmapcamera.utils.setDrawable
import com.example.gpsmapcamera.utils.setImage
import com.example.gpsmapcamera.utils.setTextColorAndBackgroundTint
import com.example.gpsmapcamera.utils.setTextColorRes
import com.example.gpsmapcamera.utils.setTintColor
import com.example.gpsmapcamera.utils.showToast
import com.example.gpsmapcamera.utils.visible
import kotlin.concurrent.timer

class CameraActivity : AppCompatActivity(),View.OnClickListener {
    private val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }


    private lateinit var cameraManager: CameraManager

    private var activeMode: Int = R.id.photo_btn
    private lateinit var micPermissionLauncher: ActivityResultLauncher<String>

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
                if ( binding.topDetailBlurView.isVisible) {
                    binding.topDetailBlurView.visibility = View.GONE
                    binding.topBlurView.visibility = View.VISIBLE
                }
            }

            setInitialStates()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()
        hideSystemBars()

        init()


    }

    private fun init()=binding.apply {

        micPermissionLauncher = registerPermissionLauncher(
            Manifest.permission.RECORD_AUDIO,
            onGranted = {
                switchMode(R.id.video_btn)

            },
            onDenied = { permanentlyDenied ->
                if (permanentlyDenied) {
                    openAppSettings()
                } else {
                }
            }
        )
        cameraManager = CameraManager(this@CameraActivity, previewView)

        requestPermissionsIfNeeded()

     /*   linearLayout.setRenderEffect(
            RenderEffect.createBlurEffect(
                20f,
                20f,
                Shader.TileMode.CLAMP
            )
        )*/
        val windowBackground = window.decorView.background

/*        topBlurView.setupWith(blurTarget)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(10f)

        topDetailBlurView.setupWith(blurTarget)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(10f)

        bottomBlurView.setupWith(blurTarget)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(10f)*/

        brightnessBar.max = 80
        brightnessBar.progress = 40
        brightnessBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Handle brightness
                val mappedValue = progress - 40
                progressText.text = mappedValue.toString()
                cameraManager.setBrightness(progress - mappedValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        switchCamBtn.setOnClickListener(this@CameraActivity)
        moreBtn.setOnClickListener(this@CameraActivity)
        x1ZoomTv.setOnClickListener(this@CameraActivity)
        x2ZoomTv.setOnClickListener(this@CameraActivity)
        shareBtn.setOnClickListener(this@CameraActivity)
        videoBtn.setOnClickListener(this@CameraActivity)
        photoBtn.setOnClickListener(this@CameraActivity)
        fileNameBtn.setOnClickListener(this@CameraActivity)
        flashBtn.setOnClickListener(this@CameraActivity)
        ratioBtn.setOnClickListener(this@CameraActivity)
        gridBtn.setOnClickListener(this@CameraActivity)
        timerBtn.setOnClickListener(this@CameraActivity)
        mirrorBtn.setOnClickListener(this@CameraActivity)
        volumeBtn.setOnClickListener(this@CameraActivity)
        focusBtn.setOnClickListener(this@CameraActivity)
        camLevelBtn.setOnClickListener(this@CameraActivity)
        brightnessBtn.setOnClickListener(this@CameraActivity)
        captureBtn.setOnClickListener(this@CameraActivity)
        videoRecordBtn.setOnClickListener(this@CameraActivity)
        videoStopBtn.setOnClickListener(this@CameraActivity)

    }

    private fun setInitialStates()=binding.apply {

        if (getCameraLevel(this@CameraActivity))
        {
            camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
            cameraLevel.setLevelEnabled(true)
        }
        else cameraLevel.setLevelEnabled(false)


        if (getCameraGrid(this@CameraActivity))
        {
            gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
            gridOverlay.updateGrid(true, 4)
        }
        else
            gridOverlay.updateGrid(false, 0)

        when(getCameraRatio(this@CameraActivity))
        {
            16->{
                ratioBtn.setDrawable(top = R.drawable.ratio16_ic)
                cameraManager.setAspectRatio(AspectRatio.RATIO_16_9)
            }
            4->{
                ratioBtn.setDrawable(top = R.drawable.ratio4_ic)
                cameraManager.setAspectRatio(AspectRatio.RATIO_4_3)
            }
        }

        if (getCameraFlash(this@CameraActivity))
        {
            flashBtn.setTintColor(R.color.blue)
            cameraManager.toggleFlash(true)
        }

        if (getCameraTimer(this@CameraActivity))
        {
            timerBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)

        }

        if (getCameraMirror(this@CameraActivity))
        {
            mirrorBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
            cameraManager.setMirror(true)
        }

        if (getCaptureSound(this@CameraActivity))
        {
            volumeBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
            cameraManager.captureSound(true)
        }

        if (getCameraLevel(this@CameraActivity))
        {
            camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
        }

        if (getAutoFocus(this@CameraActivity))
            focusBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

    }

    override fun onClick(p0: View) {

        binding.apply{
            when(p0.id)
            {
                R.id.switch_cam_btn->{
                    cameraManager.switchCamera()
                }
                R.id.file_name_btn->{
                    launchActivity<FileNameActivity> {  }
                }
                R.id.flash_btn->{
                    if (getCameraFlash(this@CameraActivity))
                    {
                        flashBtn.setTintColor(R.color.white)
                        saveCameraFlash(this@CameraActivity,false)
                        cameraManager.toggleFlash(false)
                    }
                    else
                    {
                        flashBtn.setTintColor(R.color.blue)
                        saveCameraFlash(this@CameraActivity,true)
                        cameraManager.toggleFlash(true)
                    }

                }
                R.id.ratio_btn->{
                    when(getCameraRatio(this@CameraActivity))
                    {
                        4->{
                            saveCameraRatio(this@CameraActivity,16)
                            ratioBtn.setDrawable(top = R.drawable.ratio16_ic)
                            cameraManager.setAspectRatio(AspectRatio.RATIO_16_9)

                        }
                        16->{
                            saveCameraRatio(this@CameraActivity,4)
                            ratioBtn.setDrawable(top = R.drawable.ratio4_ic)
                            cameraManager.setAspectRatio(AspectRatio.RATIO_4_3)
                        }
                    }

                }
                R.id.grid_btn->{
                    if (getCameraGrid(this@CameraActivity))
                    {
                        gridBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                        saveCameraGrid(this@CameraActivity,false)
                        gridOverlay.updateGrid(false, 0)
                    }
                    else
                    {
                        gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                        saveCameraGrid(this@CameraActivity,true)
                        gridOverlay.updateGrid(true, 4)
                    }
                }
                R.id.timer_btn->{
                    if (getCameraTimer(this@CameraActivity))
                    {
                        timerBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                        saveCameraTimer(this@CameraActivity,false)
                    }
                    else
                    {
                        timerBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                        saveCameraTimer(this@CameraActivity,true)
                    }
                }
                R.id.mirror_btn->{
                    if (getCameraMirror(this@CameraActivity))
                    {
                        mirrorBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                        saveCameraMirror(this@CameraActivity,false)
                        cameraManager.setMirror(false)
                    }
                    else
                    {
                        mirrorBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                        saveCameraMirror(this@CameraActivity,true)
                        cameraManager.setMirror(true)
                    }
                }
                R.id.volume_btn->{
                    if (getCaptureSound(this@CameraActivity))
                    {
                        volumeBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                        saveCaptureSound(this@CameraActivity,false)
                        cameraManager.captureSound(false)
                    }
                    else
                    {
                        volumeBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                        saveCaptureSound(this@CameraActivity,true)
                        cameraManager.captureSound(true)
                    }
                }
                R.id.focus_btn->{
                    if (getAutoFocus(this@CameraActivity))
                    {
                        focusBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                        saveAutoFocus(this@CameraActivity,false)
                    }
                    else
                    {
                        focusBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                        saveAutoFocus(this@CameraActivity,true)
                    }
                }
                R.id.cam_level_btn->{
                    if (getCameraLevel(this@CameraActivity))
                    {
                        camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                        saveCameraLevel(this@CameraActivity,false)
                        cameraLevel.setLevelEnabled(false)
                    }
                    else
                    {
                        camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                        saveCameraLevel(this@CameraActivity,true)
                        cameraLevel.setLevelEnabled(true)
                    }
                }
                R.id.brightness_btn->{
                    if (brightnessBarView.isVisible)
                    {
                        brightnessBarView.gone()
                        brightnessBtn.setCompoundDrawableTintAndTextColor(R.color.white,R.color.white)
                    }
                    else {
                        brightnessBarView.visible()
                        brightnessBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
                    }

                }
                R.id.capture_btn->{
//                    cameraManager.capturePhotoFromPreviewView{
//
//                    }
                    cameraManager.takePhoto() {
                        val intent = Intent(this@CameraActivity, PreviewImageActivity::class.java)
                        intent.putExtra("image_uri", it.toString())
                        startActivity(intent)
                        Toast.makeText(this@CameraActivity, "Saved: $it", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.video_record_btn->{
                    videoStopBtn.visible()
                    videoRecordBtn.gone()
                    cameraManager.startVideoRecording(
                        onStarted = {

                        },
                        onSaved = { uri ->
                            showToast("video saved:$uri")
                        },
                        onError = { msg ->
                            videoStopBtn.gone()
                            videoRecordBtn.visible()
                        }
                    )
                }
                R.id.video_stop_btn->{
                    cameraManager.stopVideoRecording()
                    videoStopBtn.gone()
                    videoRecordBtn.visible()
                }
                R.id.x1_zoom_tv->{
                    binding.x1ZoomTv.setTextColorAndBackgroundTint(R.color.black, R.color.white)
                    binding.x2ZoomTv.setTextColorAndBackgroundTint(R.color.white, R.color.transparent)
                    cameraManager.zoom1x2x(1f)

                }
                R.id.x2_zoom_tv->{
                    binding.x2ZoomTv.setTextColorAndBackgroundTint(R.color.black, R.color.white)
                    binding.x1ZoomTv.setTextColorAndBackgroundTint(R.color.white, R.color.transparent)
                    cameraManager.zoom1x2x(2f)
                }
                R.id.share_btn->{
                    switchMode(R.id.share_btn)
                }
                R.id.photo_btn->{
                    switchMode(R.id.photo_btn)
                }
                R.id.video_btn->{
                    if (isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                        switchMode(R.id.video_btn)
                    } else {
                        micPermissionLauncher.requestPermission(Manifest.permission.RECORD_AUDIO)
                    }
                }
                R.id.more_btn->{
                    if ( binding.topDetailBlurView.isVisible) {
                        binding.topDetailBlurView.visibility = View.GONE
                        binding.topBlurView.visibility = View.VISIBLE
                    }
                    else {
                        binding.topDetailBlurView.visibility = View.VISIBLE
                        binding.topBlurView.visibility = View.GONE
                    }
                }
            }
        }

    }

    private fun switchMode(newMode: Int) {
        if (activeMode == newMode) return // already active → do nothing
        activeMode = newMode

        when (newMode) {
            R.id.share_btn->{
                binding.shareBtn.setTextColorRes(R.color.blue,R.color.white,binding.photoBtn,binding.videoBtn)
                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.videoRecordBtn.visibility=View.GONE
                binding.templateBtn.visibility=View.VISIBLE
                cameraManager.setVideoRecord(false)

            }
            R.id.photo_btn->{
                binding.photoBtn.setTextColorRes(R.color.blue,R.color.white,binding.videoBtn,binding.shareBtn)
                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.videoRecordBtn.visibility=View.GONE
                binding.templateBtn.visibility=View.VISIBLE
                cameraManager.setVideoRecord(false)

            }
            R.id.video_btn->{
                binding.videoBtn.setTextColorRes(R.color.blue,R.color.white,binding.photoBtn,binding.shareBtn)
                binding.captureBtn.setImage(R.drawable.capture_in_video_ic)
                binding.videoRecordBtn.visibility=View.VISIBLE
                binding.templateBtn.visibility=View.GONE
                cameraManager.setVideoRecord(true)

            }
        }
    }

}