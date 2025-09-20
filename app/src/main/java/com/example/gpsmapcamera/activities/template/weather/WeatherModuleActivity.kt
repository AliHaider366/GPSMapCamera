package com.example.gpsmapcamera.activities.template.weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.adapters.WeatherModuleAdapter
import com.example.gpsmapcamera.databinding.ActivityWeatherModuleBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.pressureFormats
import com.example.gpsmapcamera.utils.temperatureFormats
import com.example.gpsmapcamera.utils.windFormats

class WeatherModuleActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityWeatherModuleBinding.inflate(layoutInflater)
    }

    private val fromModule by lazy {
        intent.getStringExtra(Constants.FROM_WEATHER_MODULE)?:""
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }


    private var passedList = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initList()
        setUpRV()
    }

    private fun initList() {
        when (fromModule) {
            Constants.FROM_TEMPERATURE_MODULE -> {
                passedList = temperatureFormats
            }

            Constants.FROM_WIND_MODULE -> {
                passedList = windFormats
            }
            Constants.FROM_PRESSURE_MODULE -> {
                passedList = pressureFormats
            }

        }
    }

    private fun setUpRV() = binding.run {
        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this@WeatherModuleActivity)
        val adapter =
            WeatherModuleAdapter(
                fromModule,
                passedList,
                passedTemplate
            ) { position ->
                setInt(
                    this@WeatherModuleActivity,
                    fromModule + passedTemplate,
                    position
                )
            }
        recyclerView.adapter = adapter
    }


}