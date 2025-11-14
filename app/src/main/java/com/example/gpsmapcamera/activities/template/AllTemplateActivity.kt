package com.example.gpsmapcamera.activities.template

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.TemplatePagerAdapter
import com.example.gpsmapcamera.databinding.ActivityAllTemplateBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.EventConstants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.disableView
import com.example.gpsmapcamera.utils.setTextColorAndBackgroundTint


class AllTemplateActivity : BaseActivity() {

    val binding by lazy {
        ActivityAllTemplateBinding.inflate(layoutInflater)
    }


    internal val firebaseLogger by lazy {
        (applicationContext as MyApp).firebaseEvents
    }



    var isTemplateChanged = 0

    var isTopSelected = false
    var topTemplateSelectedPosition = 0
    var stampSelectedModel = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initViews()
        setupViewPager()
        clickListeners()
    }

    private fun initViews() = binding.run {
        btnDone.disableView()
        isTopSelected = PrefManager.getBoolean(
            this@AllTemplateActivity,
            Constants.IS_TOP_TEMPLATE_SELECTED,
            false
        )
        topTemplateSelectedPosition = PrefManager.getInt(
            this@AllTemplateActivity,
            Constants.TOP_TEMPLATE_SELECTED_NUMBER, 0
        )
        stampSelectedModel = PrefManager.getString(
            this@AllTemplateActivity,
            Constants.SELECTED_STAMP_TEMPLATE,
            Constants.CLASSIC_TEMPLATE
        )
    }

    private fun setupViewPager() = binding.viewPager.run {
        // Setup ViewPager2
        val myAdapter = TemplatePagerAdapter(this@AllTemplateActivity)
        adapter = myAdapter
        isUserInputEnabled = false // disable swipe if you want only button-based navigation

        // Default tab
        setTabSelected(isBasic = true)

        // Sync tab state when user swipes (if enabled)
        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setTabSelected(isBasic = (position == 0))
            }
        })
    }

    private fun clickListeners() = binding.run {

        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }

        btnBasicTemplates.setOnClickListener {
            viewPager.currentItem = 0
            setTabSelected(isBasic = true)
        }

        btnTopTemplates.setOnClickListener {
            viewPager.currentItem = 1
            setTabSelected(isBasic = false)
        }

        btnDone.setOnClickListener {
            if (btnDone.isEnabled) {
                PrefManager.setBoolean(
                    this@AllTemplateActivity,
                    Constants.IS_TOP_TEMPLATE_SELECTED,
                    isTopSelected
                )
                PrefManager.setInt(
                    this@AllTemplateActivity,
                    Constants.TOP_TEMPLATE_SELECTED_NUMBER,
                    topTemplateSelectedPosition
                )
                PrefManager.setString(
                    this@AllTemplateActivity,
                    Constants.SELECTED_STAMP_TEMPLATE,
                    stampSelectedModel
                )

                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setTabSelected(isBasic: Boolean) {
        if (isBasic) {
            binding.btnBasicTemplates.setTextColorAndBackgroundTint(R.color.white, R.color.blue)
            binding.btnTopTemplates.setTextColorAndBackgroundTint(R.color.black, R.color.white)
        } else {
            binding.btnTopTemplates.setTextColorAndBackgroundTint(R.color.white, R.color.blue)
            binding.btnBasicTemplates.setTextColorAndBackgroundTint(R.color.black, R.color.white)
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }
}