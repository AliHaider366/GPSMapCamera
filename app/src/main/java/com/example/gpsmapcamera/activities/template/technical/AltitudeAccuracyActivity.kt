package com.example.gpsmapcamera.activities.template.technical

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.AltitudeAccuracyAdapter
import com.example.gpsmapcamera.databinding.ActivityAltitudeAccuracyBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.altitudeAccuracyFormats

class AltitudeAccuracyActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAltitudeAccuracyBinding.inflate(layoutInflater)
    }

    private val fromAltitude by lazy {
        intent.getBooleanExtra(Constants.FROM_ALTITUDE, false)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
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
        recyclerView.layoutManager = LinearLayoutManager(this@AltitudeAccuracyActivity)
        val adapter =
            AltitudeAccuracyAdapter(
                fromAltitude,
                altitudeAccuracyFormats,
                passedTemplate
            ) { position ->
                setInt(
                    this@AltitudeAccuracyActivity,
                    (if (fromAltitude) Constants.FROM_ALTITUDE else Constants.FROM_ACCURACY) + passedTemplate,
                    position
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