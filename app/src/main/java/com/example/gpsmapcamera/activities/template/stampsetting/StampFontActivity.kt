package com.example.gpsmapcamera.activities.template.stampsetting

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.MapPositionAdapter
import com.example.gpsmapcamera.adapters.StampFontAdapter
import com.example.gpsmapcamera.adapters.StampFontSizeAdapter
import com.example.gpsmapcamera.databinding.ActivityStampFontBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.stampFontList

class StampFontActivity : BaseActivity() {


    private val binding by lazy {
        ActivityStampFontBinding.inflate(layoutInflater)
    }

    private val fromStampSize by lazy {
        intent.getBooleanExtra(Constants.FROM_STAMP_SIZE, false)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }


    private val stampSizeList by lazy {
        arrayListOf<String>(
            getString(R.string.large),
            getString(R.string.medium),
            getString(R.string.small),
            getString(R.string.extra_small)
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
        recyclerView.layoutManager = LinearLayoutManager(this@StampFontActivity)

        if (fromStampSize){
            val adapter = StampFontSizeAdapter(
                stampSizeList,
                passedTemplate
            ) { position ->
                setInt(
                    this@StampFontActivity,
                    Constants.SELECTED_STAMP_SIZE + passedTemplate,
                    position
                )
            }
            recyclerView.adapter = adapter
        }else{
            val adapter = StampFontAdapter(
                stampFontList,
                passedTemplate
            ) { position ->
                setInt(
                    this@StampFontActivity,
                    Constants.SELECTED_STAMP_FONT + passedTemplate,
                    position
                )
            }
            recyclerView.adapter = adapter
        }

    }


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }



}