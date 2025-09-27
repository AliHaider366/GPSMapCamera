package com.example.gpsmapcamera.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.adapters.StampAdapter
import com.example.gpsmapcamera.adapters.StampCenterAdapter
import com.example.gpsmapcamera.cameraHelper.CameraManager
import com.example.gpsmapcamera.databinding.ActivityCameraBinding
import com.example.gpsmapcamera.databinding.StampAdvanceTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampClassicTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampReportingTemplateLayoutBinding
import com.example.gpsmapcamera.models.StampCameraPosition
import com.example.gpsmapcamera.models.StampConfig
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.models.StampPosition
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
import com.example.gpsmapcamera.utils.PrefManager.KEY_SHARE_IMAGE
import com.example.gpsmapcamera.utils.PrefManager.KEY_WHITE_BALANCE
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.PrefManager.saveBoolean
import com.example.gpsmapcamera.utils.PrefManager.saveInt
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.checkAndRequestGps
import com.example.gpsmapcamera.utils.getFontSizeFactor
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.hideSystemBars
import com.example.gpsmapcamera.utils.isPermissionGranted
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.loadGoogleMap
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
import com.example.gpsmapcamera.utils.setTintColor
import com.example.gpsmapcamera.utils.setUpMapPositionForAdvancedTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForClassicTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForReportingTemplate
import com.example.gpsmapcamera.utils.shareImage
import com.example.gpsmapcamera.utils.showToast
import com.example.gpsmapcamera.utils.stampFontList
import com.example.gpsmapcamera.utils.visible

class CameraActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    private val templateAdapterBottom = StampAdapter()
    private val templateAdapterCenter = StampCenterAdapter()
    private val templateAdapterRight = StampAdapter()

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
        enableEdgeToEdge()
        hideSystemBars()
        checkAndRequestGps(gpsResolutionLauncher)
        init()


    }

    private fun init() = binding.apply {
        setUpTemplate()
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
                    cameraManager.startCamera {
                        //set camera values after start
                        cameraManager.setBrightness(
                            getInt(
                                this@CameraActivity,
                                KEY_WHITE_BALANCE,
                                40
                            )
                        )
                    }
                    cameraManager.setupTouchControls {
                        if (binding.detailTopMenuView.isVisible) {
                            binding.detailTopMenuView.visibility = View.GONE
                            binding.defaultTopMenuView.visibility = View.VISIBLE

                            // hide brightness bar if shown
                            brightnessBarView.gone()
                            brightnessBtn.setCompoundDrawableTintAndTextColor(
                                R.color.white,
                                R.color.white
                            )
                        }
                    }
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
            cameraManager.startCamera {
                //set camera values after start
                cameraManager.setBrightness(getInt(this@CameraActivity, KEY_WHITE_BALANCE, 40))

            }
            cameraManager.setupTouchControls {
                if (binding.detailTopMenuView.isVisible) {
                    binding.detailTopMenuView.visibility = View.GONE
                    binding.defaultTopMenuView.visibility = View.VISIBLE
                    // hide brightness bar if shown
                    brightnessBarView.gone()
                    brightnessBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                }
            }
            setInitialStates()
            appViewModel.getLocation()
        }

        setClickListeners()

        brightnessValue =
            getInt(this@CameraActivity, KEY_WHITE_BALANCE, 40)        // set initial brightness
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
            switchMode(R.id.share_btn)
        }
        if (getBoolean(this@CameraActivity, KEY_CAMERA_LEVEL)) {
            camLevelBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
            cameraLevel.setLevelEnabled(true)
        } else cameraLevel.setLevelEnabled(false)


        if (getBoolean(this@CameraActivity, KEY_CAMERA_GRID)) {
            gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
            gridOverlay.updateGrid(true, 4)
        } else
            gridOverlay.updateGrid(false, 0)

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

        if (getBoolean(this@CameraActivity, KEY_CAMERA_FLASH)) {
            flashBtn.setTintColor(R.color.blue)
            cameraManager.toggleFlash(true)
        }

        if (getBoolean(this@CameraActivity, KEY_CAMERA_TIMER)) {
            when (getInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE)) {
                0 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
//                    saveCameraTimer(this@CameraActivity,0,false)
//                    saveBoolean(this@CameraActivity,KEY_CAMERA_TIMER,false)
//                    saveInt(this@CameraActivity,KEY_CAMERA_TIMER_VALUE,0)
                }

                3 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
//                    saveCameraTimer(this@CameraActivity,3,true)
                }

                5 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.red, R.color.red)
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

    private fun setClickListeners() = binding.apply {

        galleyGotoBtn.setOnClickListener {
            openLatestImageFromFolder("${appViewModel.fileSavePath}%")

        }

        switchCamBtn.setOnClickListener {
            cameraManager.switchCamera()
        }

        pickGalleyBtn.setOnClickListener {
            launchActivity<SavedPathActivity> { }
        }

        fileNameBtn.setOnClickListener {
            launchActivity<FileNameActivity> { }
        }

        flashBtn.setOnClickListener {
            if (getBoolean(this@CameraActivity, KEY_CAMERA_FLASH)) {
                flashBtn.setTintColor(R.color.white)
                saveBoolean(this@CameraActivity, KEY_CAMERA_FLASH, false)
                cameraManager.toggleFlash(false)
            } else {
                flashBtn.setTintColor(R.color.blue)
                saveBoolean(this@CameraActivity, KEY_CAMERA_FLASH, true)
                cameraManager.toggleFlash(true)
            }
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
            if (getBoolean(this@CameraActivity, KEY_CAMERA_GRID)) {
                gridBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
                saveBoolean(this@CameraActivity, KEY_CAMERA_GRID, false)
                gridOverlay.updateGrid(false, 0)
            } else {
                gridBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                saveBoolean(this@CameraActivity, KEY_CAMERA_GRID, true)
                gridOverlay.updateGrid(true, 4)
            }
        }

        timerBtn.setOnClickListener {
            when (getInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE)) {
                0 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
//                    saveCameraTimer(this@CameraActivity,3,true)
                    saveBoolean(this@CameraActivity, KEY_CAMERA_TIMER, true)
                    saveInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE, 3)
                }

                3 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.red, R.color.red)
//                    saveCameraTimer(this@CameraActivity,5,true)
                    saveBoolean(this@CameraActivity, KEY_CAMERA_TIMER, true)
                    saveInt(this@CameraActivity, KEY_CAMERA_TIMER_VALUE, 5)
                }

                5 -> {
                    timerBtn.setCompoundDrawableTintAndTextColor(R.color.white, R.color.white)
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
            } else {
                volumeBtn.setCompoundDrawableTintAndTextColor(R.color.blue, R.color.blue)
                saveBoolean(this@CameraActivity, KEY_CAPTURE_SOUND, true)
                cameraManager.captureSound(true)
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
            videoRecordBtn.gone()
            cameraManager.startVideoRecording(
                onStarted = {

                },
                onSaved = { uri ->
//                    showToast("video saved:$uri")
                },
                onError = { msg ->
                    videoStopBtn.gone()
                    videoRecordBtn.visible()
                }
            )
        }

        videoStopBtn.setOnClickListener {
            cameraManager.stopVideoRecording()
            videoStopBtn.gone()
            videoRecordBtn.visible()
        }

        x1ZoomTv.setOnClickListener {
            binding.x1ZoomTv.setTextColorAndBackgroundTint(R.color.black, R.color.white)
            binding.x2ZoomTv.setTextColorAndBackgroundTint(R.color.white, R.color.transparent)
            cameraManager.zoom1x2x(1f)
        }

        x2ZoomTv.setOnClickListener {
            binding.x2ZoomTv.setTextColorAndBackgroundTint(R.color.black, R.color.white)
            binding.x1ZoomTv.setTextColorAndBackgroundTint(R.color.white, R.color.transparent)
            cameraManager.zoom1x2x(2f)
        }

        shareBtn.setOnClickListener {
            switchMode(R.id.share_btn)

        }

        photoBtn.setOnClickListener {
            switchMode(R.id.photo_btn)
        }

        videoBtn.setOnClickListener {
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

    private fun switchMode(newMode: Int) {
        if (activeMode == newMode) return // already active → do nothing
        activeMode = newMode

        when (newMode) {
            R.id.share_btn -> {
                binding.shareBtn.setTextColorRes(
                    R.color.blue,
                    R.color.white,
                    binding.photoBtn,
                    binding.videoBtn
                )
                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.videoRecordBtn.visibility = View.GONE
                binding.templateBtn.visibility = View.VISIBLE
                cameraManager.setVideoRecord(false)
                saveBoolean(this, KEY_SHARE_IMAGE, true)

                cameraManager.stopVideoRecording()/// stop recording if started
                binding.videoStopBtn.gone()
            }

            R.id.photo_btn -> {
                binding.photoBtn.setTextColorRes(
                    R.color.blue,
                    R.color.white,
                    binding.videoBtn,
                    binding.shareBtn
                )
                binding.captureBtn.setImage(R.drawable.capture_btn_ic)
                binding.videoRecordBtn.visibility = View.GONE
                binding.templateBtn.visibility = View.VISIBLE
                cameraManager.setVideoRecord(false)
                saveBoolean(this, KEY_SHARE_IMAGE, false)

                cameraManager.stopVideoRecording()///stop recording if started
                binding.videoStopBtn.gone()

            }

            R.id.video_btn -> {
                binding.videoBtn.setTextColorRes(
                    R.color.blue,
                    R.color.white,
                    binding.photoBtn,
                    binding.shareBtn
                )
                binding.captureBtn.setImage(R.drawable.capture_in_video_ic)
                binding.videoRecordBtn.visibility = View.VISIBLE
                binding.templateBtn.visibility = View.GONE
                cameraManager.setVideoRecord(true)
                saveBoolean(this, KEY_SHARE_IMAGE, false)
            }
        }
    }

    private fun setUpTemplate() {
        selectedTemplate = PrefManager.getString(
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


            val templateType = PrefManager.getString(
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

        }
    }


    private fun setupReportingUI(allConfigs: List<StampConfig>) {


        binding.stampContainer.removeAllViews()
        binding.stampContainer.addView(reportingTemplateBinding.root)

        reportingTemplateBinding?.run {
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
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp).toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

            val baseTextSizeFortvEnv =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._9sdp).toFloat()
            tvEnvironment.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSizeFortvEnv * getScaleValue)



        }
        binding.main.setStampPosition(selectedStampPosition)

        (applicationContext as MyApp).appViewModel.getLocationAndFetch { location ->

/*            reportingTemplateBinding.map.loadGoogleMap(
                context = this,
                location = location!!,
                fragmentManager = supportFragmentManager,
                Constants.REPORTING_TEMPLATE,
            ) { googleMap ->
//            googleMapRef = googleMap
            }*/

            reportingTemplateBinding.map.loadStaticMap(
                context = this,
                location = location!!,
                Constants.CLASSIC_TEMPLATE
            )

        }

        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            reportingTemplateBinding?.map?.visible()
        } else {
            reportingTemplateBinding?.map?.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
            reportingTemplateBinding?.tvEnvironment?.visible()
            val itemIndex = PrefManager.getInt(
                this@CameraActivity,
                Constants.SELECTED_REPORTING_TAG, 0
            )
            val reportingTagSavedList =
                StampPreferences(this@CameraActivity).getWholeList(Constants.KEY_REPORTING_TAG)
            if (reportingTagSavedList.isEmpty()) {
                if (itemIndex < reportingTagsDefault.size) {
                    reportingTemplateBinding?.tvEnvironment?.text = reportingTagsDefault[itemIndex]
                }
            } else {
                if (itemIndex < reportingTagSavedList.size) {
                    reportingTemplateBinding?.tvEnvironment?.text = reportingTagSavedList[itemIndex]
                }
            }
        } else {
            reportingTemplateBinding?.tvEnvironment?.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            reportingTemplateBinding?.mapContainer?.gone()
        } else {
            reportingTemplateBinding?.mapContainer?.visible()
        }
    }

    private fun setupAdvanceUI(allConfigs: List<StampConfig>) {

        binding.stampContainer.removeAllViews()
        binding.stampContainer.addView(advanceTemplateBinding.root)


        advanceTemplateBinding?.run {
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
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp).toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)


        }

        binding.main.setStampPosition(selectedStampPosition)

        (applicationContext as MyApp).appViewModel.getLocationAndFetch { location ->

/*            advanceTemplateBinding.map.loadGoogleMap(
                context = this,
                location = location!!,
                fragmentManager = supportFragmentManager,
                Constants.ADVANCE_TEMPLATE
            ) { googleMap ->
//            googleMapRef = googleMap
            }*/

            advanceTemplateBinding.map.loadStaticMap(
                context = this,
                location = location!!,
                Constants.CLASSIC_TEMPLATE
            )

        }

        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            advanceTemplateBinding?.map?.visible()
        } else {
            advanceTemplateBinding?.map?.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
//                advanceTemplateBinding?.tvEnvironment.visible()
        } else {
//                advanceTemplateBinding?.tvEnvironment.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            advanceTemplateBinding?.mapContainer?.gone()
        } else {
            advanceTemplateBinding?.mapContainer?.visible()
        }
    }


    private fun setupClassicUI(allConfigs: List<StampConfig>) {

        binding.stampContainer.removeAllViews()
        binding.stampContainer.addView(classicTemplateBinding.root)

        classicTemplateBinding?.run {
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
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp).toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

        }

        (applicationContext as MyApp).appViewModel.getLocationAndFetch { location ->

/*            classicTemplateBinding.map.loadGoogleMap(
                context = this,
                location = location!!,
                fragmentManager = supportFragmentManager,
                Constants.CLASSIC_TEMPLATE
            ) { googleMap ->
//            googleMapRef = googleMap

            }*/
            classicTemplateBinding.map.loadStaticMap(
                context = this,
                location = location!!,
                Constants.CLASSIC_TEMPLATE
            )

            binding.main.setStampPosition(selectedStampPosition)

        }





        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            classicTemplateBinding?.map?.visible()
        } else {
            classicTemplateBinding?.map?.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
//                classicTemplateBinding?.tvEnvironment.visible()
        } else {
//                classicTemplateBinding?.tvEnvironment.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            classicTemplateBinding?.mapContainer?.gone()
        } else {
            classicTemplateBinding?.mapContainer?.visible()
        }

    }


    override fun onPause() {
        super.onPause()
        binding.detailTopMenuView.gone()
        binding.defaultTopMenuView.visible()
    }

}