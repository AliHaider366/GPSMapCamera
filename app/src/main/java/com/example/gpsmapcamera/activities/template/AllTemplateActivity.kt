package com.example.gpsmapcamera.activities.template

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.TemplatePagerAdapter
import com.example.gpsmapcamera.databinding.ActivityAllTemplateBinding
import com.example.gpsmapcamera.utils.setTextColorAndBackgroundTint


class AllTemplateActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAllTemplateBinding.inflate(layoutInflater)
    }


    var isTemplateChanged = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        setupViewPager()
        clickListeners()
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