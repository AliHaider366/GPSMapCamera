package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.ExitAdapter
import com.example.gpsmapcamera.databinding.ActivityExitBinding
import com.example.gpsmapcamera.models.ExitModel

class ExitActivity : BaseActivity() {


    private val binding by lazy {
        ActivityExitBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRV()

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

        }

        binding.mainRv.layoutManager = GridLayoutManager(this@ExitActivity, 2)
        binding.mainRv.adapter = adapter
        binding.mainRv.hasFixedSize()

    }

}