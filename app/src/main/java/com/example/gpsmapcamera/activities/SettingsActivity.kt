package com.example.gpsmapcamera.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.SettingsAdapter
import com.example.gpsmapcamera.databinding.ActivitySettingsBinding
import com.example.gpsmapcamera.enums.ImageQuality
import com.example.gpsmapcamera.objects.CameraSettingsNotifier
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.FeedbackDialog
import com.example.gpsmapcamera.utils.PrefManager.KEY_IMAGE_QUALITY
import com.example.gpsmapcamera.utils.PrefManager.KEY_TOUCH_SETTING
import com.example.gpsmapcamera.utils.PrefManager.KEY_VOLUME_BTN_SETTING
import com.example.gpsmapcamera.utils.PrefManager.getString
import com.example.gpsmapcamera.utils.PrefManager.saveString
import com.example.gpsmapcamera.utils.isSingleTouch
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.shareApp
import com.example.gpsmapcamera.utils.showDropdownMenu
import com.example.gpsmapcamera.utils.toLanguageName
import com.example.gpsmapcamera.models.SettingsModel

class SettingsActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    private lateinit var settingAdapter: SettingsAdapter


    private val settingList by lazy {
        mutableListOf(
            SettingsModel.Heading(getString(R.string.general)), // Heading item
            SettingsModel.GeneralItem(
                getString(R.string.app_language), R.drawable.setting_language_ic,
                getString(
                    this@SettingsActivity,
                    Constants.SELECTED_LANGUAGE,
                    "en-us"
                ).toLanguageName(), R.drawable.setting_next_ic
            ),
            SettingsModel.GeneralItem(
                getString(R.string.image_quality),
                R.drawable.setting_image_quality_ic,
                getString(this@SettingsActivity, KEY_IMAGE_QUALITY, getString(R.string.high)),
                R.drawable.setting_up_down_ic
            ),
            SettingsModel.GeneralItem(
                getString(R.string.volume_button),
                R.drawable.setting_volume_ic,
                getString(this@SettingsActivity, KEY_VOLUME_BTN_SETTING, getString(R.string.volume)),
                R.drawable.setting_up_down_ic),
            SettingsModel.GeneralItem(
                getString(R.string.touch_settings),
                R.drawable.setting_touch_ic,
                getString(this@SettingsActivity, KEY_TOUCH_SETTING, getString(R.string.focus)),
                R.drawable.setting_up_down_ic
            ),
            SettingsModel.Heading(getString(R.string.features)), // Heading item
//            SettingsModel.FeaturesItem(getString(R.string.watermark), R.drawable.setting_watermark_ic),
//            SettingsModel.FeaturesItem(getString(R.string.save_original_photos), R.drawable.setting_save_original_ic),
            SettingsModel.FeaturesItem(getString(R.string.qr_detection), R.drawable.setting_qr_ic),
//            SettingsModel.FeaturesItem(getString(R.string.location_share_mode), R.drawable.setting_location_share_ic),
//            SettingsModel.FeaturesItem(getString(R.string.front_rear_camera_stamp), R.drawable.setting_front_rear_ic),
            SettingsModel.Heading(getString(R.string.about)), // Heading item
            SettingsModel.AboutItem(getString(R.string.rate_us), R.drawable.setting_rate_us_ic),
            SettingsModel.AboutItem(getString(R.string.share), R.drawable.ic_share_setting),
            SettingsModel.AboutItem(
                getString(R.string.privacy_policy),
                R.drawable.setting_privacy_policy_ic
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        clickListeners()
    }


    private fun clickListeners() = binding.run {
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun init() = binding.apply {
        tvVersion.text = buildString {
            append(getString(R.string.version))
            append(" ")
            append(BuildConfig.VERSION_NAME)
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        settingAdapter = SettingsAdapter(settingList, ::onItemClick, ::onMenuClick)

        binding.rvSettings.apply {
            adapter = settingAdapter
            layoutManager = LinearLayoutManager(this@SettingsActivity)
        }

    }

    private fun onMenuClick(textView: TextView, title: String) {
        if (!isSingleTouch()) return

        val imageQuality =
            listOf(getString(R.string.low), getString(R.string.medium), getString(R.string.high))
        val touchSetting = listOf(getString(R.string.focus), getString(R.string.photo_capture))
        val volumeSetting = listOf(getString(R.string.capture_photo),getString(R.string.volume), getString(R.string.zoom))

        when (title) {
            getString(R.string.image_quality) -> {
                textView.showDropdownMenu(imageQuality) { selected ->

                    saveString(this@SettingsActivity, KEY_IMAGE_QUALITY, selected)
                    when (selected) {
                        getString(R.string.low) -> CameraSettingsNotifier.notifyQualityChanged(
                            ImageQuality.LOW
                        )

                        getString(R.string.medium) -> CameraSettingsNotifier.notifyQualityChanged(
                            ImageQuality.MEDIUM
                        )

                        getString(R.string.high) -> CameraSettingsNotifier.notifyQualityChanged(
                            ImageQuality.HIGH
                        )
                    }

                }

            }

            getString(R.string.touch_settings) -> {
                textView.showDropdownMenu(touchSetting) { selected ->

                    saveString(this@SettingsActivity, KEY_TOUCH_SETTING, selected)
                }

            }

            getString(R.string.volume_button) -> {
                textView.showDropdownMenu(volumeSetting) { selected ->

                    saveString(this@SettingsActivity, KEY_VOLUME_BTN_SETTING, selected)
                }

            }

        }

    }

    private fun onItemClick(title: String) {
        if (!isSingleTouch()) return
        when (title) {
            getString(R.string.app_language) -> {
                launchActivity<LanguageActivity>()
            }

            getString(R.string.privacy_policy) -> {
                val url = "https://weappsstudio.com/privacy-policy.html"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            getString(R.string.share) -> {
                shareApp(BuildConfig.APPLICATION_ID)
            }


            getString(R.string.rate_us) -> {
//                RateUsDialog(this@SettingsActivity).show()
                FeedbackDialog(this@SettingsActivity).show()

            }


            else -> {}

        }
    }


}