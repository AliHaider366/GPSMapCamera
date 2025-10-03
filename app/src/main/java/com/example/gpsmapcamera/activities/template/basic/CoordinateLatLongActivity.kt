package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.LatLongAdapter
import com.example.gpsmapcamera.databinding.ActivityCoordinateLatLongBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.coordinateFormats
import com.example.gpsmapcamera.utils.plusCodeFormats

class CoordinateLatLongActivity : BaseActivity() {

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
        recyclerView.layoutManager = LinearLayoutManager(this@CoordinateLatLongActivity)
        val adapter = LatLongAdapter(isFromPlusCode,if (isFromPlusCode) plusCodeFormats else coordinateFormats, passedTemplate) { position ->
            setInt(this@CoordinateLatLongActivity, if (isFromPlusCode) Constants.SELECTED_PLUS_CODE + passedTemplate else Constants.SELECTED_LAT_LONG + passedTemplate, position)
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