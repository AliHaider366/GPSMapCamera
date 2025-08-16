package com.example.gpsmapcamera.activities

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.gpsmapcamera.cameraHelper.CameraManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.ActivityCameraBinding
import com.example.gpsmapcamera.utils.hideSystemBars
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.setImage
import com.example.gpsmapcamera.utils.setTextColorAndBackgroundTint
import com.example.gpsmapcamera.utils.setTextColorRes

class CameraActivity : AppCompatActivity(),View.OnClickListener {
    private val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }


    private lateinit var cameraManager: CameraManager


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
            cameraManager.setAspectRatio(AspectRatio.RATIO_16_9)

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

        switchCamBtn.setOnClickListener(this@CameraActivity)
        moreBtn.setOnClickListener(this@CameraActivity)
        x1ZoomTv.setOnClickListener(this@CameraActivity)
        x2ZoomTv.setOnClickListener(this@CameraActivity)
        shareBtn.setOnClickListener(this@CameraActivity)
        videoBtn.setOnClickListener(this@CameraActivity)
        photoBtn.setOnClickListener(this@CameraActivity)
        fileNameBtn.setOnClickListener(this@CameraActivity)

    }

    override fun onClick(p0: View) {

        when(p0.id)
        {
            R.id.switch_cam_btn->{
                cameraManager.switchCamera()
            }
            R.id.file_name_btn->{
                launchActivity<FileNameActivity> {  }
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
                binding.shareBtn.setTextColorRes(R.color.blue,R.color.white,binding.photoBtn,binding.videoBtn)

            }
            R.id.photo_btn->{
                binding.photoBtn.setTextColorRes(R.color.blue,R.color.white,binding.videoBtn,binding.shareBtn)

                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.videoRecordBtn.visibility=View.GONE
                binding.templateBtn.visibility=View.VISIBLE

            }
            R.id.video_btn->{
                binding.videoBtn.setTextColorRes(R.color.blue,R.color.white,binding.photoBtn,binding.shareBtn)

                binding.captureBtn.setImage(R.drawable.capture_in_video_ic)
                binding.videoRecordBtn.visibility=View.VISIBLE
                binding.templateBtn.visibility=View.GONE

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