package com.example.gpsmapcamera.activities.template.technical

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.databinding.ActivityNumberingBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager

class NumberingActivity : BaseActivity() {

    private val binding by lazy {
        ActivityNumberingBinding.inflate(layoutInflater)
    }

    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initViews()
        selectedOne()
        clickListeners()
    }

    private fun clickListeners() = binding.run {

        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }

        tvMinusOne.setOnClickListener {
            PrefManager.setInt(
                this@NumberingActivity,
                Constants.NUMBERING_SEQUENCE_TYPE + passedTemplate,
                1
            )
            selectedOne()
        }

        tvPlusOne.setOnClickListener {
            PrefManager.setInt(
                this@NumberingActivity,
                Constants.NUMBERING_SEQUENCE_TYPE + passedTemplate,
                0
            )
            selectedOne()
        }

        btnReset.setOnClickListener {
            PrefManager.setInt(
                this@NumberingActivity,
                Constants.NUMBERING_SEQUENCE_TYPE + passedTemplate,
                0
            )
            selectedOne()
            etPrefix.setText("")
            PrefManager.setString(
                this@NumberingActivity,
                Constants.NUMBERING_PREFIX + passedTemplate,
                ""
            )
            etSuffix.setText("")
            PrefManager.setString(
                this@NumberingActivity,
                Constants.NUMBERING_SUFFIX + passedTemplate,
                ""
            )
            etSequenceValue.setText("")
            PrefManager.setInt(
                this@NumberingActivity,
                Constants.NUMBERING_SEQUENCE_NUMBER + passedTemplate,
                0
            )
        }
    }

    private fun selectedOne() = binding.run {

        if (PrefManager.getInt(
                this@NumberingActivity,
                Constants.NUMBERING_SEQUENCE_TYPE + passedTemplate,
                0
            ) == 0
        ) {
            tvPlusOne.setBackgroundResource(R.drawable.bg_selected_stroke_5)
            tvPlusOne.setTextColor(getColor(R.color.blue))

            tvMinusOne.setBackgroundResource(R.drawable.bg_white_stroke_5)
            tvMinusOne.setTextColor(getColor(R.color.textMainColor))

        } else {

            tvPlusOne.setBackgroundResource(R.drawable.bg_white_stroke_5)
            tvPlusOne.setTextColor(getColor(R.color.textMainColor))

            tvMinusOne.setBackgroundResource(R.drawable.bg_selected_stroke_5)
            tvMinusOne.setTextColor(getColor(R.color.blue))
        }

    }

    private fun initViews() = binding.run {

        etSequenceValue.setText(
            PrefManager.getInt(
                this@NumberingActivity,
                Constants.NUMBERING_SEQUENCE_NUMBER + passedTemplate,
                1
            ).toString()
        )
        etPrefix.setText(
            PrefManager.getString(
                this@NumberingActivity,
                Constants.NUMBERING_PREFIX + passedTemplate,
                ""
            ).toString()
        )
        etSuffix.setText(
            PrefManager.getString(
                this@NumberingActivity,
                Constants.NUMBERING_SUFFIX + passedTemplate,
                ""
            ).toString()
        )



        etPrefix.doOnTextChanged { text, _, _, _ ->
            if (text?.isNotEmpty() == true)
                PrefManager.setString(
                    this@NumberingActivity,
                    Constants.NUMBERING_PREFIX + passedTemplate,
                    text.toString()
                )
        }
        etSuffix.doOnTextChanged { text, _, _, _ ->
            if (text?.isNotEmpty() == true)
                PrefManager.setString(
                    this@NumberingActivity,
                    Constants.NUMBERING_SUFFIX + passedTemplate,
                    text.toString()
                )
        }
        etSequenceValue.doOnTextChanged { text, _, _, _ ->
            if (text?.isNotEmpty() == true)
                PrefManager.setInt(
                    this@NumberingActivity,
                    Constants.NUMBERING_SEQUENCE_NUMBER + passedTemplate,
                    text.toString().toInt()
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