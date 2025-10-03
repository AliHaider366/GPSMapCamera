package com.example.gpsmapcamera.activities.template.stampsetting

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.databinding.ActivityMapScaleBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager

class MapScaleActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMapScaleBinding.inflate(layoutInflater)
    }

    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }

    private val mapType by lazy {
        PrefManager.getString(this@MapScaleActivity, Constants.SELECTED_MAP_TYPE + passedTemplate,
            Constants.MAP_TYPE_NORMAL)
    }

    private val mapZoomLevel by lazy {
        PrefManager.getFloat(this@MapScaleActivity, Constants.SELECTED_MAP_ZOOM_LEVEL + passedTemplate, 12.5f)
    }

    private val location by lazy {
        (applicationContext as MyApp).appViewModel.getLocation()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initViews()
        setUpSeekBar()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }
    }

    private fun initViews() {
        location?.let {
            loadStaticMap(it.latitude, it.longitude, mapZoomLevel.toInt())
        }
    }

    private fun setUpSeekBar(){

        binding.zoomSeekBar.max = 100
        binding.zoomSeekBar.progress = 50 // default midpoint

        binding.zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minZoom = 5f
                val maxZoom = 20f

                // map progress (0–100) → zoom (5–20)
                val zoomLevel = minZoom + (progress / 100f) * (maxZoom - minZoom)
                binding.zoomText.text = progress.toString()

                val location = (applicationContext as MyApp).appViewModel.getLocation()
                location?.let {
                    loadStaticMap(it.latitude, it.longitude, zoomLevel.toInt())
                    PrefManager.setFloat(this@MapScaleActivity, Constants.SELECTED_MAP_ZOOM_LEVEL + passedTemplate, zoomLevel.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    private fun loadStaticMap(lat: Double, lng: Double, zoom: Int) {

        val typeParam = when (mapType) {
            Constants.MAP_TYPE_NORMAL -> "roadmap"
            Constants.MAP_TYPE_SATELLITE -> "satellite"
            Constants.MAP_TYPE_TERRAIN -> "terrain"
            Constants.MAP_TYPE_HYBRID -> "hybrid"
            else -> "roadmap"
        }

        val apiKey = "AIzaSyB9bZ09nESdvT2kRmFEAKbQ3gUqJJwOApI" // your API key
        val url = "https://maps.googleapis.com/maps/api/staticmap" +
                "?center=$lat,$lng" +
                "&zoom=$zoom" +
                "&size=640x640" +        // max allowed
                "&scale=2" +             // doubles to 1280x1280 (HD)
                "&maptype=$typeParam" +
                "&markers=color:red%7C$lat,$lng" +
                "&key=$apiKey"

        // load into ImageView (using Glide or Picasso)
        Glide.with(this)
            .load(url)
            .into(binding.mapView) // replace with your ImageView
    }


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }
}