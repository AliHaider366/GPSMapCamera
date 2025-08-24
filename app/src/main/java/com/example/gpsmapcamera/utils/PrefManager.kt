package com.example.gpsmapcamera.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH
import com.example.gpsmapcamera.utils.Constants.SAVED_FILE_NAME

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
    private const val KEY_CAMERA_TIMER_VALUE  = "camera_timer_value"
    private const val KEY_FIRST_TIME  = "first_time"
    private const val KEY_FILE_PATH  = "file_path"
    private const val KEY_FILE_NAME  = "file_name"
    private const val KEY_SHARE_IMAGE  = "share_image"
    /*file name activity*/
    private const val KEY_DAY_CHECK  = "day_check"
    private const val KEY_24HOURS_CHECK  = "24hours_check"
    private const val KEY_SEQUENCE_NUMBER_CHECK  = "24hours_check"
    private const val KEY_SEQUENCE_NUMBER_VALUE  = "24hours_value"
    private const val KEY_CUSTOM_NAME_1_CHECK  = "custom_name_1_check"
    private const val KEY_CUSTOM_NAME_1_VALUE  = "custom_name_1_value"
    private const val KEY_CUSTOM_NAME_2_CHECK  = "custom_name_2_check"
    private const val KEY_CUSTOM_NAME_2_VALUE  = "custom_name_2_value"
    private const val KEY_CUSTOM_NAME_3_CHECK  = "custom_name_3_check"
    private const val KEY_CUSTOM_NAME_3_VALUE  = "custom_name_3_value"
    private const val KEY_NOTE_CHECK  = "note_check"
    private const val KEY_NOTE_VALUE  = "note_value"
    private const val KEY_LAT_LONG_CHECK  = "lat_long_check"
    private const val KEY_DMS_CHECK  = "dms_value"
    private const val KEY_PLUS_CODE_CHECK  = "plus_code_value"
    private const val KEY_TIME_ZONE_CHECK  = "time_zone_value"


    fun saveDayCheck(context: Context, isDayChecked: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_DAY_CHECK, isDayChecked) }
    }
    fun getDayCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_DAY_CHECK, default)
    }
    fun save24HourCheck(context: Context, is24Checked: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_24HOURS_CHECK, is24Checked) }
    }
    fun get24HourCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_24HOURS_CHECK, default)
    }
    fun saveSequenceNumCheck(context: Context,seqNum:String, isSeqNumChecked: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SEQUENCE_NUMBER_CHECK, isSeqNumChecked)
            putString(KEY_SEQUENCE_NUMBER_VALUE, seqNum)}
    }
    fun getSequenceNumCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_SEQUENCE_NUMBER_CHECK, default)
    }
    fun getSequenceNumValue(context: Context, default:String ="0"): String {
        return getPrefs(context).getString(KEY_SEQUENCE_NUMBER_VALUE, default)?:""
    }
    fun saveCustomName1Check(context: Context,name:String, isNameChecked: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CUSTOM_NAME_1_CHECK, isNameChecked)
            putString(KEY_CUSTOM_NAME_1_VALUE, name)}
    }
    fun saveCustomName2Check(context: Context,name:String, isNameChecked: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CUSTOM_NAME_2_CHECK, isNameChecked)
            putString(KEY_CUSTOM_NAME_2_VALUE, name)}
    }
    fun saveCustomName3Check(context: Context,name:String, isNameChecked: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CUSTOM_NAME_3_CHECK, isNameChecked)
            putString(KEY_CUSTOM_NAME_3_VALUE, name)}
    }
    fun getCustomName1Value(context: Context, default:String =""): String {
        return getPrefs(context).getString(KEY_CUSTOM_NAME_1_VALUE, default)?:""
    }
    fun getCustomName2Value(context: Context, default:String =""): String {
        return getPrefs(context).getString(KEY_CUSTOM_NAME_2_VALUE, default)?:""
    }
    fun getCustomName3Value(context: Context, default:String =""): String {
        return getPrefs(context).getString(KEY_CUSTOM_NAME_3_VALUE, default)?:""
    }
    fun getNoteCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTE_CHECK, default)
    }
    fun getNoteValue(context: Context, default:String =""): String {
        return getPrefs(context).getString(KEY_NOTE_VALUE, default)?:""
    }
    fun getLatLongCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_LAT_LONG_CHECK, default)
    }
    fun getDMSCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_DMS_CHECK, default)
    }
    fun getPlusCodeCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_PLUS_CODE_CHECK, default)
    }
    fun getTimeZoneCheck(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_TIME_ZONE_CHECK, default)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveFilePath(context: Context, path: String) {
        getPrefs(context).edit { putString(KEY_FILE_PATH, path) }
    }
    fun getFilePath(context: Context, default: String = SAVED_DEFAULT_FILE_PATH): String {
        return getPrefs(context).getString(KEY_FILE_PATH, default)?:""
    }
    fun saveFileName(context: Context, path: String) {
        getPrefs(context).edit { putString(KEY_FILE_NAME, path) }
    }
    fun getFileName(context: Context, default: String = SAVED_FILE_NAME): String {
        return getPrefs(context).getString(KEY_FILE_NAME, default)?:""
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
    fun getWhiteBalance(context: Context, default: Int = 40): Int {
        return getPrefs(context).getInt(KEY_WHITE_BALANCE, default)
    }

    fun saveCameraRatio(context: Context, ratio: Int) {
        getPrefs(context).edit { putInt(KEY_CAMERA_RATIO, ratio) }
    }
    fun getCameraRatio(context: Context, default: Int = 16): Int {
        return getPrefs(context).getInt(KEY_CAMERA_RATIO, default)
    }

    fun saveShareImage(context: Context, isShareOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_SHARE_IMAGE, isShareOn) }
    }
    fun getShareImage(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_SHARE_IMAGE, default)
    }

    fun saveCameraTimer(context: Context,value:Int, isTimerOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_CAMERA_TIMER, isTimerOn)
            putInt(KEY_CAMERA_TIMER_VALUE, value)}
    }
    fun getCameraTimer(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_TIMER, default)
    }
    fun getCameraTimerValue(context: Context, default: Int = 0): Int {
        return getPrefs(context).getInt(KEY_CAMERA_TIMER_VALUE, default)
    }

    fun saveIsFirstTime(context: Context, isFirst: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_FIRST_TIME, isFirst) }
    }
    fun getIsFirstTime(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_TIME, default)
    }

    fun saveAutoFocus(context: Context, isFocusOn: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_AUTO_FOCUS, isFocusOn) }
    }
    fun getAutoFocus(context: Context, default: Boolean=false): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_FOCUS, default)
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