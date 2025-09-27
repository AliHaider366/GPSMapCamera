package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpsmapcamera.adapters.LanguageAdapter
import com.example.gpsmapcamera.databinding.ActivityLanguageBinding
import com.example.gpsmapcamera.utils.launchActivity
import com.example.mycam.models.Language

class LanguageActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLanguageBinding.inflate(layoutInflater)
    }

    private lateinit var languageAdapter: LanguageAdapter


    private val lanList=listOf(
        Language("en_us", "English (US)", "English", "US"),
        Language("en_uk", "English (UK)", "English", "GB"),
        Language("ca", "Canada", "English", "CA"),
        Language("ph", "Philippines", "Tagalog", "PH"),
        Language("fr", "French", "Français", "FR"),
        Language("es", "Spanish", "Español", "ES"),
        Language("de", "German", "Deutsch", "DE"),
        Language("zh", "Chinese", "中文", "CN"),
        Language("hi", "Hindi", "हिन्दी", "IN"),
        Language("pt_pt", "Portugal", "Português", "PT"),
        Language("pt_br", "Portuguese", "Português", "BR"),
        Language("ru", "Russian", "Русский", "RU")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()

        clickListeners()
    }

    private fun clickListeners() = binding.run {
        btnApply.setOnClickListener {
            launchActivity<OnBoardingActivity> {  }
            finish()
        }
    }

    private fun init()=binding.apply {

        btnApply.isEnabled=false
        btnApply.alpha=0.5f

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter(
            onLanguageSelected = { language ->
                binding.btnApply.isEnabled=true
                binding.btnApply.alpha=1f
                languageAdapter.updateSelection(language)
            },
        )

        binding.rvLanguages.apply {
            adapter = languageAdapter
            layoutManager = GridLayoutManager(this@LanguageActivity, 2)
        }

        languageAdapter.submitList(lanList)
    }

}