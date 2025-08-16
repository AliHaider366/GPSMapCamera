package com.example.gpsmapcamera.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefManager {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_IMAGE_COUNT  = "image_count"
    private const val KEY_CAPTURE_SOUND  = "capture_sound"
    private const val KEY_CAMERA_FLASH  = "camera_flash"
    private const val KEY_WHITE_BALANCE  = "white_balance"
    private const val KEY_FLIP_MODE  = "flip_mode"
    private const val KEY_CAMERA_LEVEL  = "camera_level"
    private const val KEY_AUTO_FOCUS  = "auto_focus"


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveImageCount(context: Context, volume: Int) {
        getPrefs(context).edit { putInt(KEY_IMAGE_COUNT, volume) }
    }
    fun getImageCount(context: Context, default: Int = 0): Int {
        return getPrefs(context).getInt(KEY_IMAGE_COUNT, default)
    }

    fun saveWhiteBalance(context: Context, volume: Int) {
        getPrefs(context).edit { putInt(KEY_WHITE_BALANCE, volume) }
    }
    fun getWhiteBalance(context: Context, default: Int = 0): Int {
        return getPrefs(context).getInt(KEY_WHITE_BALANCE, default)
    }

    fun saveAutoFocus(context: Context, isFocusOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_AUTO_FOCUS, isFocusOn) }
    }
    fun getAutoFocus(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_FOCUS, default)
    }

    fun saveCameraLevel(context: Context, isLevelOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_LEVEL, isLevelOn) }
    }
    fun getCameraLevel(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_LEVEL, default)
    }

    fun saveCameraFlip(context: Context, isFlipOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_FLIP_MODE, isFlipOn) }
    }
    fun getCameraFlip(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_FLIP_MODE, default)
    }

    fun saveCaptureSound(context: Context, isSoundOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAPTURE_SOUND, isSoundOn) }
    }
    fun getCaptureSound(context: Context,default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAPTURE_SOUND,default)
    }

    fun saveCameraFlash(context: Context, isFlashOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_FLASH, isFlashOn) }
    }
    fun getCameraFlash(context: Context,default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_FLASH,default)
    }

}