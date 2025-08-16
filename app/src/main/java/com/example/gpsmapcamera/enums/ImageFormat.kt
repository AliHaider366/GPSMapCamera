package com.example.gpsmapcamera.enums

import android.graphics.Bitmap

enum class ImageFormat(val extension: String, val mimeType: String, val compressFormat: Bitmap.CompressFormat) {
    JPG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG),
    PNG("png", "image/png", Bitmap.CompressFormat.PNG),
    WEBP("webp", "image/webp", Bitmap.CompressFormat.WEBP)
}