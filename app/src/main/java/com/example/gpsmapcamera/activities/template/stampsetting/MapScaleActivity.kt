package com.example.gpsmapcamera.activities.template.stampsetting

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.ActivityMapScaleBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapScaleActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMapScaleBinding.inflate(layoutInflater)
    }

    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }

    private var googleMapRef: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
        setUpSeekBar()
    }

    private fun initViews() {
        val mapFragmentTag = "map_fragment_${System.currentTimeMillis()}"
        val mapFragment = SupportMapFragment.newInstance()

        val location = (applicationContext as MyApp).appViewModel.getLocation()

        location?.let {

            supportFragmentManager.beginTransaction()
                .replace(binding.mapView.id, mapFragment, mapFragmentTag)
                .commit()

            mapFragment.getMapAsync { googleMap ->
                googleMapRef = googleMap

                val location = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.5f))
                googleMap.addMarker(MarkerOptions().position(location).title("You're here"))
                googleMap.uiSettings.setAllGesturesEnabled(false)
                googleMap.mapType = getSelectedMapType()
            }

        }

    }

    private fun getSelectedMapType() : Int{
        val mapType = PrefManager.getString(this@MapScaleActivity, Constants.SELECTED_MAP_TYPE + passedTemplate,
            Constants.MAP_TYPE_NORMAL)

        return when(mapType){
            Constants.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_NORMAL
            Constants.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
            Constants.MAP_TYPE_TERRAIN -> GoogleMap.MAP_TYPE_TERRAIN
            Constants.MAP_TYPE_HYBRID -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun setUpSeekBar(){


        binding.zoomSeekBar.max = 100
        binding.zoomSeekBar.progress = 50 // default midpoint

        binding.zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minZoom = 5f
                val maxZoom = 20f


                // map progress (0–100) → zoom (2–21)
                val zoomLevel = minZoom + (progress / 100f) * (maxZoom - minZoom)
                binding.zoomText.text = progress.toString()
                googleMapRef?.let { map ->
                    val currentLatLng = map.cameraPosition.target
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel))
                    PrefManager.setFloat(this@MapScaleActivity, Constants.SELECTED_MAP_ZOOM_LEVEL, zoomLevel)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }
}