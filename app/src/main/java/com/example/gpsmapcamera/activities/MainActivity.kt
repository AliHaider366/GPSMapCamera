package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.saved.SavedMediaActivity
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.adapters.HomeAdapter
import com.example.gpsmapcamera.databinding.ActivityMainBinding
import com.example.gpsmapcamera.models.HomeModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.EventConstants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.launchActivity

class MainActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val firebaseLogger by lazy {
        (applicationContext as MyApp).firebaseEvents
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseLogger.logEvent(
            activityName = EventConstants.LANDING_SCREEN,
            eventName =  EventConstants.EVENT_LANDING,
            parameters = mapOf(EventConstants.PARAM_SCREEN to EventConstants.PARAM_VALUE_SHOWN)
        )


        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        setupRV()
        clickListeners()
        (applicationContext as MyApp).appViewModel.getLocation()
    }

    private fun clickListeners() = binding.run {
        ivSettings.setOnClickListener {
            launchActivity<SettingsActivity>()
        }
    }

    private fun setupRV(){
        val homeList = listOf(
            HomeModel(
                title = getString(R.string.camera),
                desc = getString(R.string.home_camera_desc),
                mainIcon = R.drawable.ic_home_camera,
                bgIcon = R.drawable.ic_camera_bg,
                bg = R.drawable.bg_gradient_blue
            ),
            HomeModel(
                title = getString(R.string.video),
                desc = getString(R.string.record_with_gps_watermark),
                mainIcon = R.drawable.ic_home_video,
                bgIcon = R.drawable.ic_video_bg,
                bg = R.drawable.bg_gradient_green
            ),
            HomeModel(
                title = getString(R.string.quick_share),
                desc = getString(R.string.share_media_instantly),
                mainIcon = R.drawable.ic_home_quick_share,
                bgIcon = R.drawable.ic_quick_share_bg,
                bg = R.drawable.bg_gradient_orange
            ),
            HomeModel(
                title = getString(R.string.templates),
                desc = getString(R.string.gps_watermark_templates),
                mainIcon = R.drawable.ic_home_templates,
                bgIcon = R.drawable.ic_templates_bg,
                bg = R.drawable.bg_gradient_purple
            ),
            HomeModel(
                title = getString(R.string.saved_data),
                desc = getString(R.string.captured_photos_videos),
                mainIcon = R.drawable.ic_home_saved_data,
                bgIcon = R.drawable.ic_saved_data_bg,
                bg = R.drawable.bg_gradient_pink
            ),
        )

        val adapter = HomeAdapter(homeList) { item, position ->

            firebaseLogger.logEvent(
                activityName = EventConstants.LANDING_SCREEN,
                eventName =  EventConstants.EVENT_LANDING,
                parameters = mapOf(EventConstants.PARAM_ACTION_TYPE to item.title)
            )


            when(item.title){
                getString(R.string.camera)->{
                    launchActivity<CameraActivity>(){
                    }
                }
                getString(R.string.video)->{
                    launchActivity<CameraActivity>(){
                        putExtra(Constants.FROM_HOME_VIDEO_SELECTED, true)
                    }
                }
                getString(R.string.quick_share)->{
                    launchActivity<CameraActivity>(){
                        putExtra(Constants.FROM_HOME_QUICK_SHARE_SELECTED, true)
                    }
                }
                getString(R.string.saved_data)->{
                    launchActivity<SavedMediaActivity>()
                }
                getString(R.string.templates)->{
                    launchActivity<AllTemplateActivity>()
                }
            }
        }

        binding.mainRv.layoutManager = GridLayoutManager(this@MainActivity, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // First item full width, others half width
                    return if (position == 0) 2 else 1
                }
            }
        }
        binding.mainRv.adapter = adapter
        binding.mainRv.hasFixedSize()

    }



    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            launchActivity<ExitActivity>()
        }
    }


}