package com.example.gpsmapcamera.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.databinding.ActivitySplashBinding
import com.example.gpsmapcamera.utils.PrefManager.KEY_FIRST_TIME
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.launchActivity

class SplashActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!getBoolean(this,KEY_FIRST_TIME))
            {
                launchActivity<PermissionActivity> {  }
            }
            else launchActivity<CameraActivity> {  }

            finish()
        }, 2000) // 2000ms = 2 seconds

    }
}