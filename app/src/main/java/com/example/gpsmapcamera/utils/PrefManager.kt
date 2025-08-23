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
    private const val KEY_CAMERA_MIRROR  = "camera_mirror"
    private const val KEY_CAMERA_LEVEL  = "camera_level"
    private const val KEY_AUTO_FOCUS  = "auto_focus"
    private const val KEY_CAMERA_GRID  = "camera_grid"
    private const val KEY_CAMERA_RATIO  = "camera_ratio"
    private const val KEY_CAMERA_TIMER  = "camera_timer"
    private const val KEY_FIRST_TIME  = "first_time"


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveIsFirstTime(context: Context, isFirst: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_FIRST_TIME, isFirst) }
    }
    fun getIsFirstTime(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_TIME, default)
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

    fun saveCameraRatio(context: Context, ratio: Int) {
        getPrefs(context).edit { putInt(KEY_CAMERA_RATIO, ratio) }
    }
    fun getCameraRatio(context: Context, default: Int = 16): Int {
        return getPrefs(context).getInt(KEY_CAMERA_RATIO, default)
    }

    fun saveAutoFocus(context: Context, isFocusOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_AUTO_FOCUS, isFocusOn) }
    }
    fun getAutoFocus(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_FOCUS, default)
    }

    fun saveCameraTimer(context: Context, isTimerOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_TIMER, isTimerOn) }
    }
    fun getCameraTimer(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_TIMER, default)
    }

    fun saveCameraGrid(context: Context, isGridOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_GRID, isGridOn) }
    }
    fun getCameraGrid(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_GRID, default)
    }

    fun saveCameraLevel(context: Context, isLevelOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_LEVEL, isLevelOn) }
    }
    fun getCameraLevel(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_LEVEL, default)
    }

    fun saveCameraMirror(context: Context, isMirrorOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_MIRROR, isMirrorOn) }
    }
    fun getCameraMirror(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_MIRROR, default)
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