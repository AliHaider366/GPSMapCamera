package com.example.gpsmapcamera.activities.template.stampsetting

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.MapPositionAdapter
import com.example.gpsmapcamera.databinding.ActivityMapPositionBinding
import com.example.gpsmapcamera.models.MapType
import com.example.gpsmapcamera.models.MapTypeModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.dateFormats
import com.example.gpsmapcamera.utils.timeZoneFormats

class MapPositionActivity : BaseActivity() {


    private val binding by lazy {
        ActivityMapPositionBinding.inflate(layoutInflater)
    }

    private val fromStampPosition by lazy {
        intent.getBooleanExtra(Constants.FROM_STAMP_POSITION, false)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }

    private val mapPositionList by lazy {
        arrayListOf<String>(
            getString(R.string.left),
            getString(R.string.right),

        )
    }

    private val stampPositionList by lazy {
        arrayListOf<String>(
            getString(R.string.bottom),
            getString(R.string.top),

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
        recyclerView.layoutManager = LinearLayoutManager(this@MapPositionActivity)
        val adapter = MapPositionAdapter(
            fromStampPosition,
            if (fromStampPosition) stampPositionList else mapPositionList,
            passedTemplate
        ) { position ->
            setInt(
                this@MapPositionActivity,
                if (fromStampPosition) Constants.SELECTED_STAMP_POSITION + passedTemplate else Constants.SELECTED_MAP_POSITION + passedTemplate,
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