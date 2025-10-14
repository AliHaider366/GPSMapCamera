package com.example.gpsmapcamera.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpsmapcamera.adapters.LanguageAdapter
import com.example.gpsmapcamera.databinding.ActivityLanguageBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.LocaleHelper.setLocale
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.PrefManager.setBoolean
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.visible
import com.example.mycam.models.Language

class LanguageActivity : BaseActivity() {

    private val binding by lazy {
        ActivityLanguageBinding.inflate(layoutInflater)
    }



    private var selectedLangCode = ""

    private lateinit var languageAdapter: LanguageAdapter


    private val fromSplash by lazy {
        intent.getBooleanExtra(Constants.FROM_SPLASH, false)
    }

/*
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
*/

    private val lanList by lazy {
        ArrayList<Language>().apply {
            add(Language("en-US", "English (US)", "English", "US"))
            add(Language("fr", "Français", "French", "FR"))
            add(Language("hi", "हिन्दी (India)", "Hindi", "IN"))
            add(Language("es", "Español", "Spanish", "ES"))
            add(Language("zh", "中文", "Chinese", "CN"))
            add(Language("pt-PT", "Português (Portugal)", "Portuguese", "PT"))
            add(Language("ru", "Русский", "Russian", "RU"))
            add(Language("in", "Indonesian", "Indonesian", "ID"))
            add(Language("fil", "Philippines", "Tagalog", "PH"))
            add(Language("bn", "বাংলা", "Bengali", "BD"))
            add(Language("pt-BR", "Português (Brazil)", "Portuguese", "BR"))
            add(Language("af", "Afrikaans", "Afrikaans", "ZA"))
            add(Language("de", "Deutsch", "German", "DE"))
            add(Language("fr-CA", "Canada", "French (Canada)", "CA"))
            add(Language("en-GB", "English (UK)", "English", "GB"))
            add(Language("ko", "Korean", "Korean", "KR"))
            add(Language("nl", "Dutch", "Dutch", "NL"))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        selectedLangCode = PrefManager.getString(this, Constants.SELECTED_LANGUAGE, "")
        init()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        btnApply.setOnClickListener {
            PrefManager.setString(this@LanguageActivity, Constants.SELECTED_LANGUAGE, selectedLangCode)
            setLocale(this@LanguageActivity, selectedLangCode)
            if (fromSplash) {
                launchActivity<OnBoardingActivity> { }
            }else{
                launchActivity<CameraActivity> {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            finish()
        }
    }

    private fun init()=binding.apply {

        btnApply.isEnabled=false
        btnApply.alpha=0.5f
        setupRecyclerView()


        if (getBoolean(this@LanguageActivity, Constants.SHOW_LANGUAGE_ANIM, true)) {
            lottieView.visible()
        } else {
            lottieView.gone()
        }
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter(
            onLanguageSelected = { language ->
                setBoolean(this@LanguageActivity, Constants.SHOW_LANGUAGE_ANIM, false)
                binding.lottieView.gone()
                binding.btnApply.isEnabled=true
                binding.btnApply.alpha=1f
                languageAdapter.updateSelection(language)
                selectedLangCode = language.code
            }
        )


        binding.rvLanguages.apply {
            adapter = languageAdapter
            layoutManager = GridLayoutManager(this@LanguageActivity, 2)
        }

//        val selectedItem = lanList.find { it.code.equals(selectedLangCode, ignoreCase = true) }
        languageAdapter.submitList(lanList)

//        Log.d("TAG", "setupRecyclerView: $selectedItem")
//        selectedItem?.let {
//            languageAdapter.updateSelection(it)
//        }

    }

}