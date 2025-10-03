package com.example.gpsmapcamera.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gpsmapcamera.utils.LocaleHelper.getLanguage
import com.example.gpsmapcamera.utils.LocaleHelper.setLocale
import com.example.gpsmapcamera.utils.setPortraitOrientation
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPortraitOrientation()
        setLocale(getLanguage(this))
    }

    private fun setLocale(locale: String) {
        setLocale(this, locale)

        var localex = ""
        var isbrazilian = false
        var isCanadian = false

        if (locale == "pt-br") {
            localex = "pt"
            isbrazilian = true
        } else if (locale == "fr_ca") {
            localex = "fr"
            isCanadian = true
        } else {
            localex = locale
        }

        val newLocale = if (isbrazilian) Locale(localex, "BR") else if (isCanadian) Locale(
            localex, "CA"
        ) else if (locale == "bn") {
            localex = "bn"
            Locale("bn", "BD")
        } else Locale(localex)
        Locale.setDefault(newLocale)
        val resources = getResources()
        val dm = resources.displayMetrics
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, dm)
    }
}