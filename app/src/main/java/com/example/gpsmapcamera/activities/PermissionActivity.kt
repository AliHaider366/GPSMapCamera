package com.example.gpsmapcamera.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gpsmapcamera.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }

}