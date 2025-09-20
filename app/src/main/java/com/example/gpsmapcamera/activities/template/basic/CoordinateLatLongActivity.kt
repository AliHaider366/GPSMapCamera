package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.adapters.LatLongAdapter
import com.example.gpsmapcamera.databinding.ActivityCoordinateLatLongBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.coordinateFormats
import com.example.gpsmapcamera.utils.plusCodeFormats

class CoordinateLatLongActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCoordinateLatLongBinding.inflate(layoutInflater)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE)?: Constants.CLASSIC_TEMPLATE
    }

    private val isFromPlusCode by lazy {
        intent.getBooleanExtra(Constants.FROM_PLUS_CODE, false)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpRV()
    }

    private fun setUpRV() = binding.run {
        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this@CoordinateLatLongActivity)
        val adapter = LatLongAdapter(isFromPlusCode,if (isFromPlusCode) plusCodeFormats else coordinateFormats, passedTemplate) { position ->
            setInt(this@CoordinateLatLongActivity, if (isFromPlusCode) Constants.SELECTED_PLUS_CODE + passedTemplate else Constants.SELECTED_LAT_LONG + passedTemplate, position)
        }
        recyclerView.adapter = adapter
    }

}