package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import android.util.Patterns
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.databinding.ActivityContactNumberBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.showToast

class ContactNumberActivity : BaseActivity() {

    private val binding by lazy {
        ActivityContactNumberBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initUI()
        clickListeners()
    }

    private fun initUI() = binding.run {
        val getNumber =
            PrefManager.getString(this@ContactNumberActivity, Constants.ADDED_PHONE_NUMBER)
        if (getNumber.isNotEmpty())
            phoneNumberInput.setText(getNumber)
    }

    private fun clickListeners() = binding.run {
        addNumberButton.setOnClickListener {
            val numberText = phoneNumberInput.text.toString()
            if (isValidPhoneNumber(numberText)) {
                PrefManager.setString(
                    this@ContactNumberActivity,
                    Constants.ADDED_PHONE_NUMBER,
                    numberText
                )
                finish()
            } else {
                showToast(getString(R.string.please_enter_valid_number))
            }
        }

        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }

    }


    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotEmpty() && Patterns.PHONE.matcher(phone).matches()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }

}