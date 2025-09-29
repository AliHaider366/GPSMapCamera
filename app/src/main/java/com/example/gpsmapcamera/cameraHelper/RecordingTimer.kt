package com.example.gpsmapcamera.cameraHelper

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.util.concurrent.TimeUnit

class RecordingTimer(private val textView: TextView) {

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var updateRunnable: Runnable? = null

    fun start() {
        if (updateRunnable != null)
            return

        startTime = System.currentTimeMillis()
        updateRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                textView.text = formatTime(elapsed)
                handler.postDelayed(this, 1000) // update every second
            }
        }
        handler.post(updateRunnable!!)
    }

    fun stop() {
        updateRunnable?.let {
            handler.removeCallbacks(it)
            updateRunnable = null
        }
    }

    fun reset() {
        stop()
        textView.text = "00:00:00"
    }

    private fun formatTime(ms: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
