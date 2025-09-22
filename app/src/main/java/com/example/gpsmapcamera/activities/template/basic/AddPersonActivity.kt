package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.gpsmapcamera.databinding.ActivityAddPersonBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager

class AddPersonActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddPersonBinding.inflate(layoutInflater)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
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
}