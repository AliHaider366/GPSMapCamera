package com.example.gpsmapcamera.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.databinding.ActivityPermissionBinding
import com.example.gpsmapcamera.utils.PrefManager.KEY_FIRST_TIME
import com.example.gpsmapcamera.utils.PrefManager.saveBoolean
import com.example.gpsmapcamera.utils.arePermissionsGranted
import com.example.gpsmapcamera.utils.isPermissionGranted
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.registerMultiplePermissionsLauncher
import com.example.gpsmapcamera.utils.registerPermissionLauncher
import com.example.gpsmapcamera.utils.requestPermission
import com.example.gpsmapcamera.utils.setTextColorAndBackgroundTint
import com.example.gpsmapcamera.utils.showToast

class PermissionActivity : BaseActivity() {

    private val binding by lazy {
        ActivityPermissionBinding.inflate(layoutInflater)
    }

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var galleryPermissionLauncher: ActivityResultLauncher<Array<String>>

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val galleryPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (arePermissionsGranted(galleryPermissions))
        {
            binding.btmGalleryAllow.setTextColorAndBackgroundTint(R.color.white,R.color.blue)
            binding.btmGalleryAllow.text= getString(R.string.allowed)

        }
        if (isPermissionGranted(Manifest.permission.CAMERA))
        {
            binding.btmCameraAllow.setTextColorAndBackgroundTint(R.color.white,R.color.blue)
            binding.btmCameraAllow.text= getString(R.string.allowed)

        }
        if (arePermissionsGranted(locationPermissions))
        {
            binding.btmLocationAllow.setTextColorAndBackgroundTint(R.color.white,R.color.blue)
            binding.btmLocationAllow.text= getString(R.string.allowed)

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            backBtn.setOnClickListener {
            finish()
            }

            enableContinueButtonState()

            cameraPermissionLauncher = registerPermissionLauncher(
                Manifest.permission.CAMERA,
                onGranted = {
                    btmCameraAllow.setTextColorAndBackgroundTint(R.color.white, R.color.blue)
                    btmCameraAllow.text= getString(R.string.allowed)
                    enableContinueButtonState()
                    showToast(getString(R.string.permission_granted))
                },
                onDenied = { permanentlyDenied ->
                    if (permanentlyDenied) {
                        openAppSettings()
                    } else {
                        showToast(getString(R.string.permission_denied))

                    }
                }
            )
            locationPermissionLauncher = registerMultiplePermissionsLauncher(
                permissions = locationPermissions,
                onGranted = {
                    btmLocationAllow.setTextColorAndBackgroundTint(R.color.white, R.color.blue)
                    enableContinueButtonState()
                    btmLocationAllow.text= getString(R.string.allowed)
                    showToast(getString(R.string.permission_granted))
                },
                onDenied = { permanentlyDenied ->
                    if (permanentlyDenied) {
                        openAppSettings()
                    } else {
                        showToast(getString(R.string.permission_denied))
                    }
                }
            )
            galleryPermissionLauncher = registerMultiplePermissionsLauncher(
                permissions = galleryPermissions,
                onGranted = {
                    btmGalleryAllow.setTextColorAndBackgroundTint(R.color.white, R.color.blue)
                    enableContinueButtonState()
                    btmGalleryAllow.text= getString(R.string.allowed)
                    showToast(getString(R.string.permission_granted))
                },
                onDenied = { permanentlyDenied ->
                    if (permanentlyDenied) {
//                        showToast("Permission permanently denied")
                        openAppSettings()
                    } else {
                        showToast(getString(R.string.permission_denied))
                    }
                }
            )

            btmGalleryAllow.setOnClickListener {
//                requestGalleryPermission()
                galleryPermissionLauncher.launch(galleryPermissions)
            }
            btmCameraAllow.setOnClickListener {
//                requestCameraPermission()
                cameraPermissionLauncher.requestPermission(Manifest.permission.CAMERA)

            }
            btmLocationAllow.setOnClickListener {
//                requestLocationPermission()
                locationPermissionLauncher.launch(locationPermissions)
            }
            btnContinue.setOnClickListener {
                saveBoolean(this@PermissionActivity,KEY_FIRST_TIME,false)
                launchActivity<CameraActivity> {  }
            }
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.CAMERA) &&
                arePermissionsGranted(locationPermissions) &&
                arePermissionsGranted(galleryPermissions)
    }

    private fun enableContinueButtonState()=binding.apply {
        if (areAllPermissionsGranted())
        {
            btnContinue.isEnabled = true
            btnContinue.setTextColorAndBackgroundTint(R.color.white,R.color.blue)
        }

    }

  /*  private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun isGalleryPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ → need to check both
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            galleryPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            // Android 12 and below
            galleryPermissionLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }*/

}