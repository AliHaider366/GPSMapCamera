package com.example.gpsmapcamera.helpers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseEventLogger(private val firebaseAnalytics: FirebaseAnalytics) {

    fun logEvent(
        activityName: String? = null,
        eventName: String,
        parameters: Map<String, Any?> = emptyMap()
    ) {

        val bundle = Bundle()

        // Add optional activity/screen name
        activityName?.let {
            bundle.putString("activity_name", it)
        }

        // Add all parameters dynamically
        parameters.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> if (value != null) {
                    bundle.putString(key, value.toString())
                }
            }
        }

        // Send event to Firebase
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
