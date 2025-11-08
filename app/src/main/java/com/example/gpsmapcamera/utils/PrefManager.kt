package com.example.gpsmapcamera.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.gpsmapcamera.models.FieldItem
import org.json.JSONArray


object PrefManager {

    const val PREF_NAME = "app_prefs"
    const val KEY_IMAGE_COUNT  = "image_count"
    const val KEY_SELECTED_FOLDER_PATH  = "selected_folder_path"
    const val KEY_FILE_PATH  = "file_path"
    const val KEY_FOLDER_NAME  = "folder_name"
    const val KEY_CAPTURE_SOUND  = "capture_sound"
    const val KEY_CAMERA_FLASH  = "camera_flash"
    const val KEY_WHITE_BALANCE  = "white_balance"
    const val KEY_CAMERA_MIRROR  = "camera_mirror"
    const val KEY_CAMERA_LEVEL  = "camera_level"
    const val KEY_AUTO_FOCUS  = "auto_focus"
    const val KEY_CAMERA_GRID  = "camera_grid"
    const val KEY_CAMERA_RATIO  = "camera_ratio"
    const val KEY_CAMERA_TIMER  = "camera_timer"
    const val KEY_CAMERA_TIMER_VALUE  = "camera_timer_value"
    const val KEY_FIRST_TIME  = "first_time"
    const val KEY_FILE_NAME  = "file_name"
    const val KEY_SHARE_IMAGE  = "share_image"
    const val KEY_IMAGE_QUALITY  = "image_quality"
    const val KEY_TOUCH_SETTING  = "touch_setting"
    const val KEY_VOLUME_BTN_SETTING  = "volume_btn_setting"
    const val KEY_QR_DETECT_SETTING  = "qr_detection_setting"
    const val KEY_TYPE_COLOR  = "type_color"
    const val KEY_TYPE_BG_COLOR  = "type_bg_color"
    const val KEY_TYPE_FONT  = "type_font"
    /*file name activity*/
    const val KEY_DAY_CHECK  = "day_check"
    const val KEY_24HOURS_CHECK  = "24hours_check"
    const val KEY_SEQUENCE_NUMBER_CHECK  = "sequence_num_check"
    const val KEY_SEQUENCE_NUMBER_VALUE  = "sequence_num_value"
    const val KEY_CUSTOM_NAME_1_CHECK  = "custom_name_1_check"
    const val KEY_CUSTOM_NAME_1_VALUE  = "custom_name_1_value"
    const val KEY_CUSTOM_NAME_2_CHECK  = "custom_name_2_check"
    const val KEY_CUSTOM_NAME_2_VALUE  = "custom_name_2_value"
    const val KEY_CUSTOM_NAME_3_CHECK  = "custom_name_3_check"
    const val KEY_CUSTOM_NAME_3_VALUE  = "custom_name_3_value"
    const val KEY_NOTE_CHECK  = "note_check"
    const val KEY_NOTE_VALUE  = "note_value"
    const val KEY_ADDRESS_CHECK  = "address_check"
    const val KEY_ADDRESS_LINE1_CHECK  = "address_line1_check"
    const val KEY_ADDRESS_LINE2_CHECK  = "address_line2_check"
    const val KEY_ADDRESS_LINE3_CHECK  = "address_line3_check"
    const val KEY_ADDRESS_LINE4_CHECK  = "address_line4_check"
    const val KEY_LAT_LONG_CHECK  = "lat_long_check"
    const val KEY_DMS_CHECK  = "dms_check"
    const val KEY_PLUS_CODE_CHECK  = "plus_code_check"
    const val KEY_TIME_ZONE_CHECK  = "time_zone_check"
    const val KEY_FILENAME_PATTERN  = "filename_pattern"
    const val KEY_LAT_LONG_VALUE_INDEX  = "lat_long_value_index"
    const val KEY_PLUS_CODE_VALUE_INDEX  = "plus_code_value_index"
    const val KEY_TIME_ZONE_VALUE_INDEX  = "time_zone_value_index"
    const val KEY_DATE_VALUE_INDEX  = "date_value_index"
    const val KEY_FIELD_ORDER  = "fields_order"
    const val KEY_FOLDER_LIST = "key_folder_list"
    const val SECOND_SESSION = "SECOND_SESSION"


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveItemOrder(items: List<FieldItem>, context: Context) {
        val order = items.map { it.name } // Save titles (or unique IDs)
        val json = JSONArray(order).toString()
        getPrefs(context).edit {putString(KEY_FIELD_ORDER, json) }
    }
    fun loadItemOrder(defaultItems: List<FieldItem>, context: Context): MutableList<FieldItem> {
        val json = getPrefs(context).getString(KEY_FIELD_ORDER, null)

        return if (json != null) {
            val array = JSONArray(json)
            val order = (0 until array.length()).map { array.getString(it) }

            // Reorder default items based on saved order
            val orderedList = mutableListOf<FieldItem>()
            order.forEach { title ->
                defaultItems.find { it.name == title }?.let { orderedList.add(it) }
            }

            // Add any missing items (new ones not yet saved)
            defaultItems.forEach {
                if (!orderedList.contains(it)) orderedList.add(it)
            }

            orderedList
        } else {
            defaultItems.toMutableList()
        }
    }

    fun saveFolder(context: Context, folderName: String) {
        // Get existing list
        val currentList = getFolderList(context).toMutableList()

        // Add only if not already present
        if (!currentList.contains(folderName)) {
            currentList.add(folderName)
        }

        // Save back as JSON
        val json = JSONArray(currentList).toString()
        getPrefs(context).edit {
            putString(KEY_FOLDER_LIST, json)
        }
    }
    fun getFolderList(context: Context): List<String> {
        val json = getPrefs(context).getString(KEY_FOLDER_LIST, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePatternList(context: Context, key: String, pattern: List<String>) {
        val joined = pattern.joinToString(",")   // "DATETIME,DAY,SEQ,ADDRESS_LINE1,LATLONG"
        getPrefs(context).edit { putString(key, joined) }
    }

    /// Setter
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit { putBoolean(key, value) }
    }
    fun saveString(context: Context, key: String, value: String) {
        getPrefs(context).edit { putString(key, value) }
    }
    fun saveInt(context: Context, key: String, value: Int) {
        getPrefs(context).edit { putInt(key, value) }
    }
    // Getters






    fun getInt(context: Context, key: String, default: Int = 0): Int {
        return getPrefs(context).getInt(key, default)
    }

    fun setInt(context: Context, key: String, value: Int = 0) {
        getPrefs(context).edit { putInt(key, value) }
    }


    fun getFloat(
        context: Context,
        key: String,
        default: Float = 0f
    ): Float {
        return getPrefs(context).getFloat(key, default)
    }

    fun setFloat(context: Context, key: String, value: Float = 0f) {
        getPrefs(context).edit { putFloat(key, value) }
    }

    fun getString(
        context: Context,
        key: String,
        default: String = ""
    ): String {
        return getPrefs(context).getString(key, default) ?: ""
    }

    fun setString(context: Context, key: String, value: String = "") {
        getPrefs(context).edit { putString(key, value) }
    }

    fun getBoolean(
        context: Context,
        key: String,
        default: Boolean = false
    ): Boolean {
        return getPrefs(context).getBoolean(key, default)
    }

    fun setBoolean(
        context: Context,
        key: String,
        value: Boolean = false
    ) {
        getPrefs(context).edit { putBoolean(key, value) }
    }


}