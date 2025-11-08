package com.example.gpsmapcamera.objects

import com.example.gpsmapcamera.enums.ImageQuality
import com.example.gpsmapcamera.interfaces.CameraSettingsListener

object CameraSettingsNotifier {
    var listener: CameraSettingsListener? = null

    fun notifyQualityChanged(newQuality: ImageQuality) {
        listener?.onQualityChanged(newQuality)
    }

    fun notifyQRDetectionChanged(isEnabled:Boolean)
    {
     listener?.onQRDetectChanged(isEnabled)
    }
}
