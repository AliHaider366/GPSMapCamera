package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.SettingsAdapter
import com.example.gpsmapcamera.databinding.ActivitySettingsBinding
import com.example.gpsmapcamera.utils.launchActivity
import com.example.mycam.models.SettingsModel

class SettingsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    private lateinit var languageAdapter: SettingsAdapter


    private val settingList by lazy {
        mutableListOf(
            SettingsModel.Heading(getString(R.string.general)), // Heading item
            SettingsModel.GeneralItem(getString(R.string.app_language), R.drawable.setting_language_ic, "English",R.drawable.setting_next_ic),
            SettingsModel.GeneralItem(getString(R.string.image_quality), R.drawable.setting_image_quality_ic,"High",R.drawable.setting_up_down_ic),
            SettingsModel.GeneralItem(getString(R.string.volume_button), R.drawable.setting_volume_ic,"Capture photo",R.drawable.setting_up_down_ic),
            SettingsModel.GeneralItem(getString(R.string.touch_settings), R.drawable.setting_touch_ic,"Photo Capture",R.drawable.setting_up_down_ic),
            SettingsModel.Heading(getString(R.string.features)), // Heading item
            SettingsModel.FeaturesItem(getString(R.string.watermark), R.drawable.setting_watermark_ic),
            SettingsModel.FeaturesItem(getString(R.string.save_original_photos), R.drawable.setting_save_original_ic),
            SettingsModel.FeaturesItem(getString(R.string.qr_detection), R.drawable.setting_qr_ic),
            SettingsModel.FeaturesItem(getString(R.string.location_share_mode), R.drawable.setting_location_share_ic),
            SettingsModel.FeaturesItem(getString(R.string.front_rear_camera_stamp), R.drawable.setting_front_rear_ic),
            SettingsModel.Heading(getString(R.string.about)), // Heading item
            SettingsModel.AboutItem(getString(R.string.rate_us), R.drawable.setting_rate_us_ic),
            SettingsModel.AboutItem(getString(R.string.share), R.drawable.setting_share_ic),
            SettingsModel.AboutItem(getString(R.string.privacy_policy), R.drawable.setting_privacy_policy_ic),
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

    private fun init()=binding.apply {

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        languageAdapter = SettingsAdapter(settingList, ::onItemClick)

        binding.rvSettings.apply {
            adapter = languageAdapter
            layoutManager = LinearLayoutManager(this@SettingsActivity)
        }

    }

    private fun onItemClick(title : String){
        when(title){
            getString(R.string.app_language)->{
                launchActivity<LanguageActivity>()
            }


            else->{}

        }
    }


}