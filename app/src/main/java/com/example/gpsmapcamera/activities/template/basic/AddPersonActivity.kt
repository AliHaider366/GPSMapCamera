package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.databinding.ActivityAddPersonBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager

class AddPersonActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAddPersonBinding.inflate(layoutInflater)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initViews()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }
    }

    private fun initViews() = binding.run {

        val personName = PrefManager.getString(
            this@AddPersonActivity,
            Constants.SELECTED_PERSON_NAME + passedTemplate,
            ""
        )

        if (personName.isNotEmpty())
            etMain.setText(personName)


        etMain.doOnTextChanged { text, _, _, _ ->
            PrefManager.setString(
                this@AddPersonActivity,
                Constants.SELECTED_PERSON_NAME + passedTemplate,
                text.toString()
            )
        }
    }


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }
}