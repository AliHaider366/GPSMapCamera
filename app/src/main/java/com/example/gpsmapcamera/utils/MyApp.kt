package com.example.gpsmapcamera.utils

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.gpsmapcamera.helpers.FirebaseEventLogger
import com.example.gpsmapcamera.viewModels.AppViewModel
import com.google.firebase.analytics.FirebaseAnalytics

class MyApp:Application() {

    val appViewModel: AppViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(AppViewModel::class.java)
    }

    val firebaseEvents by lazy {
        FirebaseEventLogger(FirebaseAnalytics.getInstance(this))
    }

    companion object {
        var isColdStart = true
        var lastActivityStopped = false
        var startType: String = "unknown"
    }


    lateinit var mapApiKey: String
        private set

    override fun onCreate() {
        super.onCreate()
        startType = "cold"


        mapApiKey = getMapsApiKey()
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    }
}