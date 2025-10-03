package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.FullAddressAdapter
import com.example.gpsmapcamera.databinding.ActivityFullAddressBinding
import com.example.gpsmapcamera.models.AddressModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.updateAddressPref


class FullAddressActivity : BaseActivity() {

    private val binding by lazy {
        ActivityFullAddressBinding.inflate(layoutInflater)
    }

    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE)?: Constants.CLASSIC_TEMPLATE
    }


    private val dataList by lazy {
        ArrayList<AddressModel>().apply {
            add(AddressModel(getString(R.string.city), PrefManager.getBoolean(this@FullAddressActivity,
                Constants.FULL_ADDRESS_CITY + passedTemplate, true)))
            add(AddressModel(getString(R.string.state), PrefManager.getBoolean(this@FullAddressActivity,
                Constants.FULL_ADDRESS_STATE + passedTemplate, true)))
            add(AddressModel(getString(R.string.country), PrefManager.getBoolean(this@FullAddressActivity,
                Constants.FULL_ADDRESS_COUNTRY + passedTemplate, true)))
            add(AddressModel(getString(R.string.pincode), PrefManager.getBoolean(this@FullAddressActivity,
                Constants.FULL_ADDRESS_PIN_CODE + passedTemplate, true)))
        }
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
        recyclerView.layoutManager = LinearLayoutManager(this@FullAddressActivity)
        val adapter =
            FullAddressAdapter(dataList) { item ->
                updateAddressPref(item,passedTemplate)
            }
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }




}
