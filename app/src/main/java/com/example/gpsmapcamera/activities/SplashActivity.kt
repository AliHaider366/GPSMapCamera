package com.example.gpsmapcamera.activities

import android.annotation.SuppressLint
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
import com.example.gpsmapcamera.utils.PrefManager.SECOND_SESSION
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.launchActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            if (getBoolean(this,KEY_FIRST_TIME, true))
            {
                launchActivity<LanguageActivity> {  }
            }
            else if (getBoolean(this, SECOND_SESSION, true))
            {
                launchActivity<StartMenuActivity> {  }
            } else launchActivity<CameraActivity> {  }
            finish()
        }, 5000) // 2000ms = 2 seconds

    }
}