package com.example.gpsmapcamera.utils

import com.example.gpsmapcamera.BuildConfig
import java.util.Date

object Constants {

    val SAVED_DEFAULT_FILE_PATH="DCIM/Camera"
    val CUSTOM_SAVED_FILE_PATH_ROOT="DCIM/${BuildConfig.APPLICATION_ID}"
    val SAVED_FILE_NAME="${Date().formatForFile()}.jpg"
}