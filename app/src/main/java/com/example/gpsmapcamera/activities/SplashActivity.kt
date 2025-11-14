package com.example.gpsmapcamera.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.gpsmapcamera.databinding.ActivitySplashBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.EventConstants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.KEY_FIRST_TIME
import com.example.gpsmapcamera.utils.PrefManager.SECOND_SESSION
import com.example.gpsmapcamera.utils.PrefManager.THIRD_SESSION
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.launchActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    private val firebaseLogger by lazy {
        (applicationContext as MyApp).firebaseEvents
    }

    private var splashStartTime = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseLogger.logEvent(
            activityName = EventConstants.SPLASH_SCREEN,
            eventName =  EventConstants.EVENT_SPLASH,
            parameters = mapOf(EventConstants.PARAM_SCREEN to EventConstants.PARAM_VALUE_SHOWN)
        )

        moveNext(savedInstanceState)

    }


    private fun moveNext(savedInstanceState: Bundle?) {

        Handler(Looper.getMainLooper()).postDelayed({

            // 1. Detect start type
            val startType = detectStartType(savedInstanceState)

            // 2. Calculate splash time
            val totalTimeSeconds =
                (System.currentTimeMillis() - splashStartTime) / 1000.0

            // 3. Send Firebase analytics
            sendSplashAnalytics(startType, totalTimeSeconds)



            if (getBoolean(this,KEY_FIRST_TIME, true))
            {
                launchActivity<LanguageActivity> {
                    putExtra(Constants.FROM_SPLASH, true)
                }
            }
            else if (getBoolean(this, SECOND_SESSION, true))
            {
                launchActivity<StartMenuActivity> {  }
            }
            else if (getBoolean(this, THIRD_SESSION, true))
            {
                launchActivity<ThirdSessionActivity> {  }
            } else launchActivity<MainActivity> {  }
            finish()
        }, 5000) // 5000ms = 5 seconds

    }


    private fun detectStartType(savedInstanceState: Bundle?): String {
        val type = when {
            MyApp.isColdStart -> {
                MyApp.isColdStart = false
                EventConstants.PARAM_COLD
            }
            savedInstanceState != null -> {
                EventConstants.PARAM_WARM
            }
            MyApp.lastActivityStopped -> {
                EventConstants.PARAM_HOT
            }
            else -> EventConstants.PARAM_WARM
        }

        MyApp.startType = type
        return type
    }

    private fun sendSplashAnalytics(startType: String, seconds: Double) {

        val params = when (startType) {
            EventConstants.PARAM_COLD -> mapOf(
                EventConstants.PARAM_START to EventConstants.PARAM_COLD,
                EventConstants.PARAM_COLD to seconds
            )
            EventConstants.PARAM_WARM -> mapOf(
                EventConstants.PARAM_START to EventConstants.PARAM_WARM,
                EventConstants.PARAM_WARM to seconds
            )
            EventConstants.PARAM_HOT -> mapOf(
                EventConstants.PARAM_START to EventConstants.PARAM_HOT,
                EventConstants.PARAM_HOT to seconds
            )
            else -> emptyMap()
        }

        firebaseLogger.logEvent(
            activityName = EventConstants.SPLASH_SCREEN,
            eventName = EventConstants.EVENT_SPLASH_TIME,
            parameters = params
        )


        firebaseLogger.logEvent(
            activityName = EventConstants.SPLASH_SCREEN,
            eventName = EventConstants.EVENT_SPLASH,
            parameters = mapOf(EventConstants.PARAM_START to startType)
        )

    }

    override fun onStop() {
        super.onStop()
        MyApp.lastActivityStopped = true
    }

    override fun onResume() {
        super.onResume()
        MyApp.lastActivityStopped = false
    }
}