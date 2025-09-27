package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.adapters.DateFormatAdapter
import com.example.gpsmapcamera.databinding.ActivityDateFormatBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.dateFormats
import com.example.gpsmapcamera.utils.timeZoneFormats

class DateFormatActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityDateFormatBinding.inflate(layoutInflater)
    }

    private val fromTimeZone by lazy {
        intent.getBooleanExtra(Constants.FROM_TIME_ZONE,false)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE)?: Constants.CLASSIC_TEMPLATE
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
        recyclerView.layoutManager = LinearLayoutManager(this@DateFormatActivity)
        val adapter = DateFormatAdapter(fromTimeZone,if (fromTimeZone) timeZoneFormats else dateFormats, passedTemplate) { position ->
            Log.d("TAG", "formatDateTemplate: fromTimeZone $fromTimeZone")
            Log.d("TAG", "formatDateTemplate: $passedTemplate")
            setInt(this@DateFormatActivity, if (fromTimeZone) Constants.TIME_ZONE_SELECTED_FORMAT + passedTemplate else Constants.DATE_TIME_SELECTED_FORMAT + passedTemplate, position)
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