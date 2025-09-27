package com.example.gpsmapcamera.activities.template.stampsetting

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.technical.AltitudeAccuracyActivity
import com.example.gpsmapcamera.adapters.AltitudeAccuracyAdapter
import com.example.gpsmapcamera.adapters.MapTypeAdapter
import com.example.gpsmapcamera.databinding.ActivityMapTypeBinding
import com.example.gpsmapcamera.models.MapType
import com.example.gpsmapcamera.models.MapTypeModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.PrefManager.setString
import com.example.gpsmapcamera.utils.altitudeAccuracyFormats
import com.example.gpsmapcamera.utils.saveSelectedMapType

class MapTypeActivity : AppCompatActivity() {


    private val binding by lazy {
        ActivityMapTypeBinding.inflate(layoutInflater)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }

    private val mapTypeList by lazy {
        arrayListOf<MapTypeModel>(
            MapTypeModel(getString(R.string.normal), R.drawable.map_type_normal, MapType.NORMAL),
            MapTypeModel(getString(R.string.satellite), R.drawable.map_type_satellite, MapType.SATELLITE),
            MapTypeModel(getString(R.string.terrain), R.drawable.map_type_terrain, MapType.TERRAIN),
            MapTypeModel(getString(R.string.hybrid), R.drawable.map_type_hybrid, MapType.HYBRID)
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        setUpRV()
        clickListeners()
    }


    private fun clickListeners() = binding.run {
        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }
    }


    private fun setUpRV() = binding.run {
        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this@MapTypeActivity)
        val adapter =
            MapTypeAdapter(
                mapTypeList,
                passedTemplate
            ) { position ->
                setString(
                    this@MapTypeActivity,
                    Constants.SELECTED_MAP_TYPE + passedTemplate,
                    position.saveSelectedMapType()
                )
            }
        recyclerView.adapter = adapter
    }


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }

}

