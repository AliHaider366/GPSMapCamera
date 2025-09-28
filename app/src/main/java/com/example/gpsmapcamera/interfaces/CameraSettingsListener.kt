package com.example.gpsmapcamera.interfaces

import com.example.gpsmapcamera.enums.ImageQuality

interface CameraSettingsListener {
    fun onQualityChanged(newQuality: ImageQuality)
}