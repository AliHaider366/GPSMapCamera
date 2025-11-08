package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.saved.SavedMediaActivity
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.adapters.ExitAdapter
import com.example.gpsmapcamera.databinding.ActivityExitBinding
import com.example.gpsmapcamera.models.ExitModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.launchActivity

class ExitActivity : BaseActivity() {


    private val binding by lazy {
        ActivityExitBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRV()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        tvContinue.setOnClickListener {
            finish()
        }
        tvExitApp.setOnClickListener {
            finish()
            finishAffinity()
        }
    }

    private fun setupRV(){
        val homeList = listOf(
            ExitModel(
                title = getString(R.string.camera),
                desc = getString(R.string.home_camera_desc),
                mainIcon = R.drawable.ic_home_camera
            ),
            ExitModel(
                title = getString(R.string.video),
                desc = getString(R.string.record_with_gps_watermark),
                mainIcon = R.drawable.ic_home_video
            ),
            ExitModel(
                title = getString(R.string.quick_share),
                desc = getString(R.string.share_media_instantly),
                mainIcon = R.drawable.ic_home_quick_share
            ),
            ExitModel(
                title = getString(R.string.templates),
                desc = getString(R.string.gps_watermark_templates),
                mainIcon = R.drawable.ic_home_templates
            )
        )

        val adapter = ExitAdapter(homeList) { item, position ->
            when(item.title){
                getString(R.string.camera)->{
                    launchActivity<CameraActivity>(){}
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

                getString(R.string.templates)->{
                    launchActivity<AllTemplateActivity>()
                }
            }
            finish()
        }

        binding.mainRv.layoutManager = GridLayoutManager(this@ExitActivity, 2)
        binding.mainRv.adapter = adapter
        binding.mainRv.hasFixedSize()

    }

}