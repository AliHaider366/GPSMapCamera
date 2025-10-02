package com.example.gpsmapcamera.utils

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.gpsmapcamera.viewModels.AppViewModel

class MyApp:Application() {

    val appViewModel: AppViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(AppViewModel::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}