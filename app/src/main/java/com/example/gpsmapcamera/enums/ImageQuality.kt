package com.example.gpsmapcamera.enums

import android.util.Size
import androidx.camera.core.resolutionselector.ResolutionStrategy

enum class ImageQuality(val qualityValue: Int, val resolution: Size, val fallbackRule: Int) {
    LOW(50, Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER),       // 480p, compressed at 50%
    MEDIUM(75, Size(1280, 720),ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER),   // 720p, compressed at 75%
    HIGH(100, Size(1920, 1080),ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER)    // 1080p, compressed at 100%
}
