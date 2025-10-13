package com.example.gpsmapcamera.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.adapters.StampAdapter
import com.example.gpsmapcamera.adapters.StampCenterAdapter
import com.example.gpsmapcamera.cameraHelper.CameraManager
import com.example.gpsmapcamera.cameraHelper.RecordingTimer
import com.example.gpsmapcamera.databinding.ActivityCameraBinding
import com.example.gpsmapcamera.databinding.StampAdvanceTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampClassicTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampReportingTemplateLayoutBinding
import com.example.gpsmapcamera.enums.ImageQuality
import com.example.gpsmapcamera.models.StampCameraPosition
import com.example.gpsmapcamera.models.StampConfig
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.models.StampPosition
import com.example.gpsmapcamera.interfaces.CameraSettingsListener
import com.example.gpsmapcamera.objects.CameraSettingsNotifier
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.PrefManager.KEY_AUTO_FOCUS
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_FLASH
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_GRID
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_LEVEL
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_MIRROR
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_RATIO
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_TIMER
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAMERA_TIMER_VALUE
import com.example.gpsmapcamera.utils.PrefManager.KEY_CAPTURE_SOUND
import com.example.gpsmapcamera.utils.PrefManager.KEY_IMAGE_QUALITY
import com.example.gpsmapcamera.utils.PrefManager.KEY_SHARE_IMAGE
import com.example.gpsmapcamera.utils.PrefManager.KEY_TOUCH_SETTING
import com.example.gpsmapcamera.utils.PrefManager.KEY_WHITE_BALANCE
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.PrefManager.getString
import com.example.gpsmapcamera.utils.PrefManager.saveBoolean
import com.example.gpsmapcamera.utils.PrefManager.saveInt
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.checkAndRequestGps
import com.example.gpsmapcamera.utils.getFontSizeFactor
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.hideSystemBars
import com.example.gpsmapcamera.utils.isPermissionGranted
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.loadStaticMap
import com.example.gpsmapcamera.utils.openAppSettings
import com.example.gpsmapcamera.utils.openLatestImageFromFolder
import com.example.gpsmapcamera.utils.registerGpsResolutionLauncher
import com.example.gpsmapcamera.utils.registerMultiplePermissionsLauncher
import com.example.gpsmapcamera.utils.registerPermissionLauncher
import com.example.gpsmapcamera.utils.reportingTagsDefault
import com.example.gpsmapcamera.utils.requestPermission
import com.example.gpsmapcamera.utils.setCompoundDrawableTintAndTextColor
import com.example.gpsmapcamera.utils.setDrawable
import com.example.gpsmapcamera.utils.setImage
import com.example.gpsmapcamera.utils.setStampPosition
import com.example.gpsmapcamera.utils.setTextColorAndBackgroundTint
import com.example.gpsmapcamera.utils.setTextColorRes
import com.example.gpsmapcamera.utils.setUpMapPositionForAdvancedTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForClassicTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForReportingTemplate
import com.example.gpsmapcamera.utils.shareImage
import com.example.gpsmapcamera.utils.showToast
import com.example.gpsmapcamera.utils.stampFontList
import com.example.gpsmapcamera.utils.visible
import java.util.concurrent.TimeUnit
import androidx.core.view.isGone
import com.example.gpsmapcamera.utils.LocaleHelper
import com.example.gpsmapcamera.utils.disableClicks
import com.example.gpsmapcamera.utils.enableClicks
import com.example.gpsmapcamera.utils.invisible
import com.example.gpsmapcamera.utils.setDelayedClickListener
import com.example.gpsmapcamera.utils.setTintColor
import java.util.Locale

class CameraActivity : BaseActivity(), CameraSettingsListener {
    private val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    private val templateAdapterBottom = StampAdapter()
    private val templateAdapterCenter = StampCenterAdapter()
    private val templateAdapterRight = StampAdapter()

    lateinit var recordingTimer: RecordingTimer

    private val classicTemplateBinding by lazy {
        StampClassicTemplateLayoutBinding.inflate(layoutInflater)
    }
    private val advanceTemplateBinding by lazy {
        StampAdvanceTemplateLayoutBinding.inflate(layoutInflater)
    }
    private val reportingTemplateBinding by lazy {
        StampReportingTemplateLayoutBinding.inflate(layoutInflater)
    }

    // Activity Result Launcher
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setUpTemplate()
            }
        }

    private var selectedStampPosition = StampCameraPosition.TOP
    private var selectedTemplate = Constants.CLASSIC_TEMPLATE


    private lateinit var cameraManager: CameraManager
    private val appViewModel by lazy {
        (application as MyApp).appViewModel
    }
    private var activeMode: Int = R.id.photo_btn
    private lateinit var micPermissionLauncher: ActivityResultLauncher<String>
    var brightnessValue = 0
    var isZoom1x = true

    val requestCode = 1001
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun allPermissionsGranted(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val gpsResolutionLauncher by lazy {
        registerGpsResolutionLauncher(
            onEnabled = {
                appViewModel.getLocation()
//                Toast.makeText(this, "GPS Enabled ", Toast.LENGTH_SHORT).show()
            },
            onDenied = {
//                Toast.makeText(this, "GPS Denied ", Toast.LENGTH_SHORT).show()
            }
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        CameraSettingsNotifier.listener = this
        enableEdgeToEdge()
        hideSystemBars()
        checkAndRequestGps(gpsResolutionLauncher)
        init()
    }

    private fun init() = binding.apply {
        setUpTemplate()
        recordingTimer = RecordingTimer(videoTimmerTV)

        val imageCaptureQuality =
            when (getString(this@CameraActivity, KEY_IMAGE_QUALITY, getString(R.string.high))) {
                getString(R.string.low) -> ImageQuality.LOW
                getString(R.string.medium) -> ImageQuality.MEDIUM
                getString(R.string.high) -> ImageQuality.HIGH
                else -> {
                    ImageQuality.HIGH
                }
            }

        cameraManager = CameraManager(this@CameraActivity, previewView)

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
        if (!allPermissionsGranted()) {
            registerMultiplePermissionsLauncher(
                permissions = PERMISSIONS,
                onGranted = {
                    cameraManager.startCamera(imageQuality = imageCaptureQuality) {
                        //set camera values after start
                        cameraManager.setBrightness(
                            getInt(
                                this@CameraActivity,
                                KEY_WHITE_BALANCE,
                                40
                            )
                        )
                    }
                    setupCameraTouch()

                    setInitialStates()
                },
                onDenied = { permanentlyDenied ->
                    if (permanentlyDenied) {
                        openAppSettings()
                    } else {
                        showToast(getString(R.string.permission_denied))
                    }
                }
            )
        } else {
            cameraManager.startCamera(imageQuality = imageCaptureQuality) {
                //set camera values after start
                cameraManager.setBrightness(getInt(this@CameraActivity, KEY_WHITE_BALANCE, 0))

            }
            setupCameraTouch()

            setInitialStates()
            appViewModel.getLocation()
        }

        setClickListeners()

        brightnessValue =
            getInt(this@CameraActivity, KEY_WHITE_BALANCE, 0)        // set initial brightness
        brightnessBar.max = 80
        brightnessBar.progress = brightnessValue + 40
        progressText.text = brightnessValue.toString()
        brightnessBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Handle brightness
                val mappedValue = progress - 40
                progressText.text = mappedValue.toString()
                cameraManager.setBrightness(mappedValue)
                saveInt(this@CameraActivity, KEY_WHITE_BALANCE, mappedValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    private fun setInitialStates() = binding.apply {


        if (getBoolean(this@CameraActivity, KEY_SHARE_IMAGE)) {
            switchMode(R.id.share_btn,false)
        }
        if (getBoolean(this@CameraActivity, KEY_CAMERA_LEVEL)) {
            camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
            cameraLevel.setLevelEnabled(true)
        } else cameraLevel.setLevelEnabled(false)


        /*        if (getBoolean(this@CameraActivity, KEY_CAMERA_GRID)) {
                    gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                    gridOverlay.updateGrid(true, 4)
                } else
                    gridOverlay.updateGrid(false, 0)*/

        when (getInt(this@CameraActivity, KEY_CAMERA_GRID)) {
            0 -> {
                saveInt(this@CameraActivity, KEY_CAMERA_GRID, 0)
                gridBtn.setDrawable(top = R.drawable.grid_3_ic)
                gridBtn.text = getString(R.string.grid)
                gridBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                gridOverlay.updateGrid(true, 0)

            }

            3 -> {
                saveInt(this@CameraActivity, KEY_CAMERA_GRID, 3)
                gridBtn.setDrawable(top = R.drawable.grid_3_ic)
                gridBtn.text = getString(R.string.grid_3_3)
                gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                gridOverlay.updateGrid(true, 3)
            }

            4 -> {
                saveInt(this@CameraActivity, KEY_CAMERA_GRID, 4)
                gridBtn.setDrawable(top = R.drawable.grid_4_ic)
                gridBtn.text = getString(R.string.grid_4_4)
                gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                gridOverlay.updateGrid(true, 4)
            }

            -3 -> {
                saveInt(this@CameraActivity, KEY_CAMERA_GRID, -3)
                gridBtn.setDrawable(top = R.drawable.phi_grid_ic)
                gridBtn.text = getString(R.string.grid_phi)
                gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                gridOverlay.updateGrid(true, 3, true)
            }
        }


        when (getInt(this@CameraActivity, KEY_CAMERA_RATIO, 16)) {
            16 -> {
                ratioBtn.setDrawable(top = R.drawable.ratio16_ic)
                cameraManager.setAspectRatio(AspectRatio.RATIO_16_9)
            }

            4 -> {
                ratioBtn.setDrawable(top = R.drawable.ratio4_ic)
                cameraManager.setAspectRatio(AspectRatio.RATIO_4_3)
            }
        }

        /*    if (getBoolean(this@CameraActivity, KEY_CAMERA_FLASH)) {
                flashBtn.setTintColor(R.color.blue)
                cameraManager.toggleFlash(true)
            }*/

        when (getInt(this@CameraActivity, KEY_CAMERA_FLASH, 0)) {
            0 -> {
                flashBtn.setImage(R.drawable.flash_off_ic)
                flashBtn.setTintColor(R.color.white)
                cameraManager.toggleFlash(0)  /*flash off*/
            }

            1 -> {
                flashBtn.setImage(R.drawable.flash_on_ic)
                flashBtn.setTintColor(R.color.blue)
                cameraManager.toggleFlash(1) /*flash on*/

            }

            2 -> {
                flashBtn.setImage(R.drawable.flash_auto_ic)
                flashBtn.setTintColor(R.color.blue)
                cameraManager.toggleFlash(2) /*flash auto*/

            }

            3 ->{
                flashBtn.setImage(R.drawable.flash_torch_ic)
                flashBtn.setTintColor(R.color.blue)
                cameraManager.toggleFlash(3)    /*flash torch*/

            }
        }


        if (getBoolean(this@CameraActivity, KEY_CAMERA_TIMER)) {
            when (getInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE)) {
                0 -> {
//                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                    timerBtn.setCompoundDrawableTintAndTextColor(textColorRes = R.color.white)
                    timerBtn.setDrawable(top = R.drawable.timer_ic)
                    timerBtn.text = getString(R.string.timer_off)

//                    saveCameraTimer(this@CameraActivity,0,false)
//                    saveBoolean(this@CameraActivity,KEY_CAMERA_TIMER,false)
//                    saveInt(this@CameraActivity,KEY_CAMERA_TIMER_VALUE,0)
                }

                3 -> {
//                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                    timerBtn.setCompoundDrawableTintAndTextColor(textColorRes = R.color.blue)
                    timerBtn.setDrawable(top = R.drawable.timer_3s_ic)
                    timerBtn.text = getString(R.string.timer_3sec)
//                    saveCameraTimer(this@CameraActivity,3,true)
                }

                5 -> {
//                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.red, R.color.red)
                    timerBtn.setCompoundDrawableTintAndTextColor(textColorRes = R.color.blue)
                    timerBtn.setDrawable(top = R.drawable.timer_5sec_ic)
                    timerBtn.text = getString(R.string.timer_5sec)
//                    saveCameraTimer(this@CameraActivity,5,true)
                }
            }
        }

        if (getBoolean(this@CameraActivity, KEY_CAMERA_MIRROR)) {
            mirrorBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
            cameraManager.setMirror(true)
        }

        if (getBoolean(this@CameraActivity, KEY_CAPTURE_SOUND)) {
            volumeBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
            cameraManager.captureSound(true)
        }

//        if (getCameraLevel(this@CameraActivity))
//        {
//            camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue,R.color.blue)
//        }

        if (getBoolean(this@CameraActivity, KEY_AUTO_FOCUS))
            focusBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

    }

    @SuppressLint("MissingPermission")
    private fun setClickListeners() = binding.apply {

        galleyGotoBtn.setOnClickListener {
            openLatestImageFromFolder("${appViewModel.fileSavePath}%")

        }

        switchCamBtn.setOnClickListener {
            cameraManager.switchCamera()
        }

        pickGalleyBtn.setOnClickListener {
            launchActivity<FileNameActivity> { }
        }

        fileNameBtn.setOnClickListener {
            launchActivity<SavedPathActivity> { }
        }

        textBtn.setOnClickListener {

        }

        flashBtn.setOnClickListener {
            /*         if (getBoolean(this@CameraActivity, KEY_CAMERA_FLASH)) {
                         flashBtn.setTintColor(R.color.white)
                         saveBoolean(this@CameraActivity, KEY_CAMERA_FLASH, false)
                         cameraManager.toggleFlash(false)
                     } else {
                         flashBtn.setTintColor(R.color.blue)
                         saveBoolean(this@CameraActivity, KEY_CAMERA_FLASH, true)
                         cameraManager.toggleFlash(true)
                     }*/

            updateFlash()
        }

        ratioBtn.setOnClickListener {
            when (getInt(this@CameraActivity, KEY_CAMERA_RATIO, 16)) {
                4 -> {
                    saveInt(this@CameraActivity, KEY_CAMERA_RATIO, 16)
                    ratioBtn.setDrawable(top = R.drawable.ratio16_ic)
                    cameraManager.setAspectRatio(AspectRatio.RATIO_16_9)

                }

                16 -> {
                    saveInt(this@CameraActivity, KEY_CAMERA_RATIO, 4)
                    ratioBtn.setDrawable(top = R.drawable.ratio4_ic)
                    cameraManager.setAspectRatio(AspectRatio.RATIO_4_3)
                }
            }
        }

        gridBtn.setOnClickListener {
            /* if (getBoolean(this@CameraActivity, KEY_CAMERA_GRID)) {
                 gridBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                 saveBoolean(this@CameraActivity, KEY_CAMERA_GRID, false)
                 gridOverlay.updateGrid(false, 0)
             } else {
                 gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                 saveBoolean(this@CameraActivity, KEY_CAMERA_GRID, true)
                 gridOverlay.updateGrid(true, 4)
             }*/
            when (getInt(this@CameraActivity, KEY_CAMERA_GRID)) {
                0 -> {
                    saveInt(this@CameraActivity, KEY_CAMERA_GRID, 3)
                    gridBtn.setDrawable(top = R.drawable.grid_3_ic)
                    gridBtn.text = getString(R.string.grid_3_3)
                    gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                    gridOverlay.updateGrid(true, 3)

                }

                3 -> {
                    saveInt(this@CameraActivity, KEY_CAMERA_GRID, 4)
                    gridBtn.setDrawable(top = R.drawable.grid_4_ic)
                    gridBtn.text = getString(R.string.grid_4_4)
                    gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                    gridOverlay.updateGrid(true, 4)
                }

                4 -> {
                    saveInt(this@CameraActivity, KEY_CAMERA_GRID, -3)
                    gridBtn.setDrawable(top = R.drawable.phi_grid_ic)
                    gridBtn.text = getString(R.string.grid_phi)
                    gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                    gridOverlay.updateGrid(true, 3, true)
                }

                -3 -> {
                    saveInt(this@CameraActivity, KEY_CAMERA_GRID, 0)
                    gridBtn.setDrawable(top = R.drawable.grid_3_ic)
                    gridBtn.text = getString(R.string.grid)
                    gridBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                    gridOverlay.updateGrid(true, 0)
                }
            }
        }

        timerBtn.setOnClickListener {
            when (getInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE)) {
                0 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(textColorRes = R.color.blue)
                    timerBtn.setDrawable(top = R.drawable.timer_3s_ic)
                    timerBtn.text = getString(R.string.timer_3sec)

//                    saveCameraTimer(this@CameraActivity,3,true)
                    saveBoolean(this@CameraActivity, KEY_CAMERA_TIMER, true)
                    saveInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE, 3)
                }

                3 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(textColorRes = R.color.blue)
                    timerBtn.setDrawable(top = R.drawable.timer_5sec_ic)
                    timerBtn.text = getString(R.string.timer_5sec)

//                    saveCameraTimer(this@CameraActivity,5,true)
                    saveBoolean(this@CameraActivity, KEY_CAMERA_TIMER, true)
                    saveInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE, 5)
                }

                5 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(textColorRes = R.color.white)
                    timerBtn.setDrawable(top = R.drawable.timer_ic)
                    timerBtn.text = getString(R.string.timer_off)
//                    saveCameraTimer(this@CameraActivity,0,false)
                    saveBoolean(this@CameraActivity, KEY_CAMERA_TIMER, false)
                    saveInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE, 0)
                }
            }
        }

        mirrorBtn.setOnClickListener {
            if (getBoolean(this@CameraActivity, KEY_CAMERA_MIRROR)) {
                mirrorBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                saveBoolean(this@CameraActivity, KEY_CAMERA_MIRROR, false)
                cameraManager.setMirror(false)
            } else {
                mirrorBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                saveBoolean(this@CameraActivity, KEY_CAMERA_MIRROR, true)
                cameraManager.setMirror(true)
            }
        }

        volumeBtn.setOnClickListener {
            if (getBoolean(this@CameraActivity, KEY_CAPTURE_SOUND)) {
                volumeBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                saveBoolean(this@CameraActivity, KEY_CAPTURE_SOUND, false)
                cameraManager.captureSound(false)
                volumeBtn.text = getString(R.string.volume_off)
            } else {
                volumeBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                saveBoolean(this@CameraActivity, KEY_CAPTURE_SOUND, true)
                cameraManager.captureSound(true)
                volumeBtn.text = getString(R.string.volume_on)
            }
        }

        focusBtn.setOnClickListener {
            if (getBoolean(this@CameraActivity, KEY_AUTO_FOCUS)) {
                focusBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                saveBoolean(this@CameraActivity, KEY_AUTO_FOCUS, false)
            } else {
                focusBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                saveBoolean(this@CameraActivity, KEY_AUTO_FOCUS, true)
            }
        }

        camLevelBtn.setOnClickListener {
            if (getBoolean(this@CameraActivity, KEY_CAMERA_LEVEL)) {
                camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                saveBoolean(this@CameraActivity, KEY_CAMERA_LEVEL, false)
                cameraLevel.setLevelEnabled(false)
            } else {
                camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                saveBoolean(this@CameraActivity, KEY_CAMERA_LEVEL, true)
                cameraLevel.setLevelEnabled(true)
            }
        }

        brightnessBtn.setOnClickListener {
            if (brightnessBarView.isVisible) {
                brightnessBarView.gone()
                brightnessBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
            } else {
                brightnessBarView.visible()
                brightnessBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
            }
        }

        captureBtn.setOnClickListener {
            if (getBoolean(this@CameraActivity, KEY_CAMERA_TIMER)) {
                /*                cameraManager.takePhotoWithTimer(
                                    getInt(
                                        this@CameraActivity,
                                        KEY_CAMERA_TIMER_VALUE
                                    ), binding.timmerTV
                                ) {
                                    val intent = Intent(this@CameraActivity, PreviewImageActivity::class.java)
                                    intent.putExtra("image_uri", it.toString())
                //                            startActivity(intent)
                                    if (getBoolean(this@CameraActivity, KEY_SHARE_IMAGE)) {
                                        it?.let { shareImage(it) }
                                    }
                                }*/

                cameraManager.takePhotoWithTimer(
                    getInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE),
                    binding.timmerTV,
                    stampContainer,// pass your overlay container
                    selectedStampPosition
                ) { uri ->
                    uri?.let {
                        val intent = Intent(this@CameraActivity, PreviewImageActivity::class.java)
                        intent.putExtra("image_uri", it.toString())
                        if (getBoolean(this@CameraActivity, KEY_SHARE_IMAGE)) {
                            shareImage(it)
                        }
                    }
                }
            } else {
                cameraManager.takePhotoWithStamp(stampContainer, selectedStampPosition) { uri ->
                    if (uri != null) {
                        val intent = Intent(this@CameraActivity, PreviewImageActivity::class.java)
                        intent.putExtra("image_uri", uri.toString())
//                            startActivity(intent)
                        if (getBoolean(this@CameraActivity, KEY_SHARE_IMAGE)) {
                            it?.let { shareImage(uri) }
                        }
                    }

                }
            }

        }

        videoRecordBtn.setOnClickListener {
            videoStopBtn.visible()
            videoTimmerTV.visible()
            videoRecordBtn.gone()

            disableClicks(shareBtn, photoBtn, videoBtn, galleyGotoBtn, templateBtn, moreBtn, flashBtn, pickGalleyBtn, fileNameBtn, settingBtn)

            cameraManager.startVideoRecordingWithStamp(
                stampContainer = stampContainer,
                stampPosition = selectedStampPosition,
                onStarted = {
                    Log.d("TAG", "setClickListeners: onStarted")
                    recordingTimer.start()
                },
                onSaved = { uri ->
                    Log.d("TAG", "setClickListeners: onSaved $uri")
//                    showToast("video saved:$uri")
                },
                onError = { msg ->
                    Log.d("TAG", "setClickListeners: onError $msg")
                    videoStopBtn.gone()
                    videoRecordBtn.visible()
                }
            )
        }

        videoStopBtn.setOnClickListener {
            enableClicks(shareBtn, photoBtn, videoBtn, galleyGotoBtn, templateBtn, moreBtn, flashBtn, pickGalleyBtn, fileNameBtn, settingBtn)
            cameraManager.stopVideoRecordingWithStamp()
            recordingTimer.stop()

            videoStopBtn.gone()
            videoTimmerTV.gone()
            videoRecordBtn.visible()
            saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 2)
            updateFlash()
        }

        x1ZoomTv.setOnClickListener {
            isZoom1x = true
            setZoom()
        }

        x2ZoomTv.setOnClickListener {
            isZoom1x = false
            setZoom()
        }

        shareBtn.setDelayedClickListener {
            switchMode(R.id.share_btn)

        }

        photoBtn.setDelayedClickListener {
            switchMode(R.id.photo_btn)
        }

        videoBtn.setDelayedClickListener {
            if (isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                switchMode(R.id.video_btn)
            } else {
                micPermissionLauncher.requestPermission(Manifest.permission.RECORD_AUDIO)
            }
        }

        moreBtn.setOnClickListener {
            if (binding.detailTopMenuView.isVisible) {
                binding.detailTopMenuView.visibility = View.GONE
                binding.defaultTopMenuView.visibility = View.VISIBLE
            } else {
                binding.detailTopMenuView.visibility = View.VISIBLE
//                binding.defaultTopMenuView.visibility = View.GONE
            }
        }

        settingBtn.setOnClickListener {
            launchActivity<SettingsActivity>() {}
        }

        templateBtn.setOnClickListener {
            activityLauncher.launch(Intent(this@CameraActivity, AllTemplateActivity::class.java))
        }
    }

    private fun updateFlash() = binding.run {

        when (getInt(this@CameraActivity, KEY_CAMERA_FLASH, 0)) {
            0 -> {
                 if (activeMode == R.id.video_btn) {
                     flashBtn.setImage(R.drawable.flash_torch_ic)
                     flashBtn.setTintColor(R.color.blue)
                     saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 3)
                     cameraManager.toggleFlash(3)
                     return
            }
                flashBtn.setImage(R.drawable.flash_on_ic)
                flashBtn.setTintColor(R.color.blue)
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 1)
                cameraManager.toggleFlash(1)  /*flash on*/
               /* if (activeMode == R.id.video_btn) {
                    cameraManager.camera?.cameraControl?.enableTorch(true) // Enable torch for video
                }*/
            }

            1 -> {

                flashBtn.setImage(R.drawable.flash_auto_ic)
                flashBtn.setTintColor(R.color.blue)
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 2)
                cameraManager.toggleFlash(2) /*flash auto*/
               /* if (activeMode == R.id.video_btn) {
                    cameraManager.camera?.cameraControl?.enableTorch(true) // Enable torch for video
                }*/

            }

            2 -> {

                flashBtn.setImage(R.drawable.flash_torch_ic)
                flashBtn.setTintColor(R.color.blue)
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 3)
                cameraManager.toggleFlash(3)

            }

            3 ->{

                flashBtn.setImage(R.drawable.flash_off_ic)
                flashBtn.setTintColor(R.color.white)
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 0)
                cameraManager.toggleFlash(0) /*flash off*/
                /*  if (activeMode == R.id.video_btn) {
                      cameraManager.camera?.cameraControl?.enableTorch(false) // Enable torch for video
                  }*/
            }
        }
    }

    private fun setZoom() {
        if (isZoom1x) {
            binding.x1ZoomTv.setTextColorAndBackgroundTint(R.color.black, R.color.white)
            binding.x2ZoomTv.setTextColorAndBackgroundTint(R.color.white, R.color.transparent)
            cameraManager.zoom1x2x(1f)
        } else {
            binding.x2ZoomTv.setTextColorAndBackgroundTint(R.color.black, R.color.white)
            binding.x1ZoomTv.setTextColorAndBackgroundTint(R.color.white, R.color.transparent)
            cameraManager.zoom1x2x(2f)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {

                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
               /* if (volumeButtonForCapture) {
                    cameraManager.takePhoto {
                        Toast.makeText(this, "Saved: $it", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cameraManager.zoomOut()
                }*/
                return true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun switchMode(newMode: Int,resetFlash:Boolean=true) {
        if (activeMode == newMode) return // already active → do nothing
        activeMode = newMode

        when (newMode) {
            R.id.share_btn -> {
                isZoom1x = true
                binding.shareBtn.setTextColorRes(
                    R.color.blue,
                    R.color.white,
                    binding.photoBtn,
                    binding.videoBtn
                )
//                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.captureBtn.visible()
                binding.videoRecordBtn.gone()
                binding.templateBtn.visible()
                cameraManager.setVideoRecord(false)
                saveBoolean(this, KEY_SHARE_IMAGE, true)

                cameraManager.stopVideoRecording()/// stop recording if started
                binding.videoStopBtn.gone()
                if (resetFlash)
                {
                    saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 3)
                    updateFlash()
                }
            }

            R.id.photo_btn -> {
                isZoom1x = true
                binding.photoBtn.setTextColorRes(
                    R.color.blue,
                    R.color.white,
                    binding.videoBtn,
                    binding.shareBtn
                )
//                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.captureBtn.visible()
                binding.videoRecordBtn.gone()
                binding.templateBtn.visible()
                cameraManager.setVideoRecord(false)
                saveBoolean(this, KEY_SHARE_IMAGE, false)

                cameraManager.stopVideoRecording()///stop recording if started
                binding.videoStopBtn.gone()
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 3)
                updateFlash()

            }

            R.id.video_btn -> {
                isZoom1x = true
                binding.videoBtn.setTextColorRes(
                    R.color.blue,
                    R.color.white,
                    binding.photoBtn,
                    binding.shareBtn
                )
//                binding.captureBtn.setImage(R.drawable.capture_in_video_ic)
                binding.captureBtn.invisible()
//                binding.templateBtn.gone()
                binding.videoRecordBtn.visible()
                cameraManager.setVideoRecord(true)
                saveBoolean(this, KEY_SHARE_IMAGE, false)
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 3)
                updateFlash()
            }
        }
        setZoom()
    }

    private fun setupCameraTouch() = binding.apply {
        cameraManager.setupTouchControls { x, y ->
            Log.d("TAG", "setupCameraTouch: ${binding.detailTopMenuView.isVisible}")

            if (binding.detailTopMenuView.isVisible) {
                binding.detailTopMenuView.visibility = View.GONE
                binding.defaultTopMenuView.visibility = View.VISIBLE

                // hide brightness bar if shown
                brightnessBarView.gone()
                brightnessBtn.setCompoundDrawableTintAndTextColor(
                    R.color.white,
                    R.color.white
                )
            } else {
                when (getString(
                    this@CameraActivity,
                    KEY_TOUCH_SETTING,
                    getString(R.string.focus)
                )) {
                    getString(R.string.focus) -> {
                        val point = cameraManager.previewView.meteringPointFactory.createPoint(x, y)
                        val action = FocusMeteringAction.Builder(point)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        cameraManager.camera?.cameraControl?.startFocusAndMetering(action)
                    }

                    getString(R.string.photo_capture) -> {
                        Log.d(
                            "TAG",
                            "setupCameraTouch: binding.videoRecordBtn.isGone ${binding.videoRecordBtn.isGone}"
                        )
                        Log.d(
                            "TAG",
                            "setupCameraTouch: binding.videoStopBtn.isGone ${binding.videoStopBtn.isGone}"
                        )
                        if (binding.videoRecordBtn.isGone && binding.videoStopBtn.isGone) {
                            /// only allow touch capture when in photo mode not in record

                            if (getBoolean(this@CameraActivity, KEY_CAMERA_TIMER)) {

                                cameraManager.takePhotoWithTimer(
                                    getInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE),
                                    binding.timmerTV,
                                    stampContainer,// pass your overlay container
                                    selectedStampPosition
                                ) { uri ->
                                    uri?.let {
                                        val intent = Intent(
                                            this@CameraActivity,
                                            PreviewImageActivity::class.java
                                        )
                                        intent.putExtra("image_uri", it.toString())
                                        if (getBoolean(this@CameraActivity, KEY_SHARE_IMAGE)) {
                                            shareImage(it)
                                        }
                                    }
                                }
                            } else {
                                cameraManager.takePhotoWithStamp(
                                    stampContainer,
                                    selectedStampPosition
                                ) { uri ->
                                    if (uri != null) {
                                        if (getBoolean(this@CameraActivity, KEY_SHARE_IMAGE)) {
                                            shareImage(uri)
                                        }
                                    }

                                }
                            }
                        }

                    }

                }

            }
        }
    }

    private fun setUpTemplate() {
        selectedTemplate = getString(
            this@CameraActivity, Constants.SELECTED_STAMP_TEMPLATE,
            Constants.CLASSIC_TEMPLATE
        )

        selectedStampPosition = if (PrefManager.getInt(
                this@CameraActivity,
                Constants.SELECTED_STAMP_POSITION + selectedTemplate,
                0
            ) == 0
        ) StampCameraPosition.TOP else StampCameraPosition.BOTTOM

        // Observe the appropriate LiveData based on the selected template
        val stampConfigs = when (selectedTemplate) {
            Constants.CLASSIC_TEMPLATE -> appViewModel.classicStampConfigs
            Constants.ADVANCE_TEMPLATE -> appViewModel.advanceStampConfigs
            Constants.REPORTING_TEMPLATE -> appViewModel.reportingStampConfigs
            else -> appViewModel.classicStampConfigs // Fallback to classic
        }

        stampConfigs.observe(this) { allConfigs ->
            val bottomItems =
                allConfigs.filter { it.position == StampPosition.BOTTOM && it.visibility }
            val centerItems =
                allConfigs.filter { it.position == StampPosition.CENTER && it.visibility }
            val rightItems =
                allConfigs.filter { it.position == StampPosition.RIGHT && it.visibility }

            templateAdapterBottom.submitList(bottomItems as ArrayList)
            templateAdapterCenter.submitList(centerItems as ArrayList)
            templateAdapterRight.submitList(rightItems as ArrayList)


            val templateType = getString(
                this@CameraActivity, Constants.SELECTED_STAMP_TEMPLATE,
                Constants.CLASSIC_TEMPLATE
            )


            // Inflate selected layout into FrameLayout
            when (templateType) {
                Constants.CLASSIC_TEMPLATE -> {
                    setupClassicUI(allConfigs)
                }

                Constants.ADVANCE_TEMPLATE -> {
                    setupAdvanceUI(allConfigs)
                }

                Constants.REPORTING_TEMPLATE -> {
                    setupReportingUI(allConfigs)
                }
            }


        }

        appViewModel.dynamicValues.observe(this) { newDynamics ->
            templateAdapterBottom.updateDynamics(newDynamics)
            templateAdapterCenter.updateDynamics(newDynamics)
            templateAdapterRight.updateDynamics(newDynamics)

            reportingTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress
            advanceTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress
            classicTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress

        }
    }


    private fun setupReportingUI(allConfigs: List<StampConfig>) {


        binding.stampContainer.removeAllViews()
        binding.stampContainer.addView(reportingTemplateBinding.root)

        reportingTemplateBinding.run {
            rvBottom.adapter = templateAdapterBottom
            rvCenter.adapter = templateAdapterCenter
            rvRight.adapter = templateAdapterRight


            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + selectedTemplate,
                    0
                )]
            )
            tvCenterTitle.typeface = typeface
            tvEnvironment.typeface = typeface

            setUpMapPositionForReportingTemplate(this@run)

            val getScaleValue = root.context.getFontSizeFactor(selectedTemplate)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp)
                    .toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

            val baseTextSizeFortvEnv =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._9sdp).toFloat()
            tvEnvironment.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                baseTextSizeFortvEnv * getScaleValue
            )


        }
        binding.main.setStampPosition(selectedStampPosition)

        (applicationContext as MyApp).appViewModel.getLocationAndFetch { location ->

            reportingTemplateBinding.map.loadStaticMap(
                context = this,
                location = location!!,
                Constants.REPORTING_TEMPLATE
            )

        }

        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            reportingTemplateBinding.map.visible()
        } else {
            reportingTemplateBinding.map.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
            reportingTemplateBinding.tvEnvironment.visible()
            val itemIndex = PrefManager.getInt(
                this@CameraActivity,
                Constants.SELECTED_REPORTING_TAG, 0
            )
            val reportingTagSavedList =
                StampPreferences(this@CameraActivity).getWholeList(Constants.KEY_REPORTING_TAG)
            if (reportingTagSavedList.isEmpty()) {
                if (itemIndex < reportingTagsDefault.size) {
                    reportingTemplateBinding.tvEnvironment.text = reportingTagsDefault[itemIndex]
                }
            } else {
                if (itemIndex < reportingTagSavedList.size) {
                    reportingTemplateBinding.tvEnvironment.text = reportingTagSavedList[itemIndex]
                }
            }
        } else {
            reportingTemplateBinding.tvEnvironment.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            reportingTemplateBinding.mapContainer.gone()
        } else {
            reportingTemplateBinding.mapContainer.visible()
        }

        if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
            reportingTemplateBinding.ivLogo.visible()
        } else {
            reportingTemplateBinding.ivLogo.gone()
        }
    }

    private fun setupAdvanceUI(allConfigs: List<StampConfig>) {

        binding.stampContainer.removeAllViews()
        binding.stampContainer.addView(advanceTemplateBinding.root)


        advanceTemplateBinding.run {
            rvBottom.adapter = templateAdapterBottom
            rvCenter.adapter = templateAdapterCenter
            rvRight.adapter = templateAdapterRight


            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + selectedTemplate,
                    0
                )]
            )
            tvCenterTitle.typeface = typeface

            setUpMapPositionForAdvancedTemplate(this@run)

            val getScaleValue = root.context.getFontSizeFactor(selectedTemplate)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp)
                    .toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)


        }

        binding.main.setStampPosition(selectedStampPosition)

        (applicationContext as MyApp).appViewModel.getLocationAndFetch { location ->

            advanceTemplateBinding.map.loadStaticMap(
                context = this,
                location = location!!,
                Constants.ADVANCE_TEMPLATE
            )

        }

        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            advanceTemplateBinding.map.visible()
        } else {
            advanceTemplateBinding.map.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
//                advanceTemplateBinding.tvEnvironment.visible()
        } else {
//                advanceTemplateBinding.tvEnvironment.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            advanceTemplateBinding.mapContainer.gone()
        } else {
            advanceTemplateBinding.mapContainer.visible()
        }

        if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
            advanceTemplateBinding.ivLogo.visible()
        } else {
            advanceTemplateBinding.ivLogo.gone()
        }
    }


    private fun setupClassicUI(allConfigs: List<StampConfig>) {

        binding.stampContainer.removeAllViews()
        binding.stampContainer.addView(classicTemplateBinding.root)

        classicTemplateBinding.run {
            rvBottom.adapter = templateAdapterBottom
            rvCenter.adapter = templateAdapterCenter
            rvRight.adapter = templateAdapterRight


            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + selectedTemplate,
                    0
                )]
            )
            tvCenterTitle.typeface = typeface

            setUpMapPositionForClassicTemplate(this@run)

            val getScaleValue = root.context.getFontSizeFactor(selectedTemplate)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp)
                    .toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

        }

        (applicationContext as MyApp).appViewModel.getLocationAndFetch { location ->

            classicTemplateBinding.map.loadStaticMap(
                context = this,
                location = location!!,
                Constants.CLASSIC_TEMPLATE
            )

            binding.main.setStampPosition(selectedStampPosition)

        }





        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            classicTemplateBinding.map.visible()
        } else {
            classicTemplateBinding.map.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
//                classicTemplateBinding.tvEnvironment.visible()
        } else {
//                classicTemplateBinding.tvEnvironment.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            classicTemplateBinding.mapContainer.gone()
        } else {
            classicTemplateBinding.mapContainer.visible()
        }

        if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
            classicTemplateBinding.ivLogo.visible()
        } else {
            classicTemplateBinding.ivLogo.gone()
        }

    }


    override fun onPause() {
        super.onPause()
        binding.apply {
            detailTopMenuView.gone()
            defaultTopMenuView.visible()

//            stop recording
            enableClicks(shareBtn, photoBtn, videoBtn, galleyGotoBtn, templateBtn, moreBtn, flashBtn, pickGalleyBtn, fileNameBtn, settingBtn)
            cameraManager.stopVideoRecording()
            cameraManager.stopVideoRecordingWithStamp()

//            saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 2)
//            updateFlash()
            if (activeMode == R.id.video_btn) {
                flashBtn.setImage(R.drawable.flash_off_ic)
                flashBtn.setTintColor(R.color.white)
                saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 0)
                cameraManager.toggleFlash(0) /*flash off*/

            }
            else{
                if (getInt(this@CameraActivity, KEY_CAMERA_FLASH, 0)==3)
                {   /*turn off the flash*/
                    flashBtn.setImage(R.drawable.flash_off_ic)
                    flashBtn.setTintColor(R.color.white)
                    saveInt(this@CameraActivity, KEY_CAMERA_FLASH, 0)
                    cameraManager.toggleFlash(0) /*flash off*/
                }
            }

            recordingTimer.stop()
            videoStopBtn.gone()
            videoTimmerTV.gone()
//            videoRecordBtn.visible()
        }

    }

    override fun onQualityChanged(newQuality: ImageQuality) {
        cameraManager.startCamera(newQuality)
    }


    private var backPressedTime = 0L
    private val exitInterval = 2000L // 2 seconds

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (backPressedTime + exitInterval > System.currentTimeMillis()) {
                finish()
                finishAffinity()
            } else {
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.tap_again_to_exit),
                    Toast.LENGTH_SHORT
                ).show()
            }
            backPressedTime = System.currentTimeMillis()

        }
    }

}