package com.example.gpsmapcamera.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpsmapcamera.enums.FilePart
import com.example.gpsmapcamera.models.AddressModel
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH
import com.example.gpsmapcamera.utils.Constants.SAVED_FILE_NAME
import com.example.gpsmapcamera.utils.PrefManager.KEY_24HOURS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_DMS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILENAME_PATTERN
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILE_NAME
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILE_PATH
import com.example.gpsmapcamera.utils.PrefManager.KEY_FOLDER_NAME
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.PrefManager.getString
import com.example.gpsmapcamera.utils.PrefManager.saveString
import com.example.gpsmapcamera.utils.formatForFile
import com.example.gpsmapcamera.utils.getCurrentAddress
import com.example.gpsmapcamera.utils.getCurrentDay
import com.example.gpsmapcamera.utils.getCurrentLatLong
import com.example.gpsmapcamera.utils.getCurrentPlusCode
import com.example.gpsmapcamera.utils.toDMSPair
import kotlinx.coroutines.launch
import java.util.Date

class AppViewModel( application: Application) : AndroidViewModel(application) {

    var fileSavePath=SAVED_DEFAULT_FILE_PATH
        private set
    var saveFileName= SAVED_FILE_NAME
        private set

    private val context = getApplication<Application>()

    var plusCode= ""
        private set

    var latLong = 0.0 to 0.0
        private set
    var latLongDMS = "0" to "0"
        private set

    var address=AddressModel()
        private set

    init {
        saveFileName= getString(context,KEY_FILE_NAME,SAVED_FILE_NAME)
        fileSavePath= getString(context,KEY_FILE_PATH,SAVED_DEFAULT_FILE_PATH)
    }

    fun getLocation(){
        viewModelScope.launch {
            plusCode = context.getCurrentPlusCode()
            address= context.getCurrentAddress()
            latLong=context.getCurrentLatLong()
            latLongDMS=latLong.toDMSPair()
        }
    }

    fun setFileName(name: String) {
        saveFileName = name
        saveString(context,KEY_FILE_NAME,name)       ///save file name in pref
    }
    fun setFileSavedPath(path: String,folderName: String) {
        fileSavePath = path
        saveString(context,KEY_FILE_PATH,path)      /// save file path in pref
        saveString(context,KEY_FOLDER_NAME,folderName)    /// save folder name in pref
    }



    fun parseSavedParts(pattern: List<FilePart>, savedFileName: String): List<String> {
        val tokens = savedFileName.removeSuffix(".jpg").split("_")
        val parts = mutableListOf<String>()
        var i = 0

        for (part in pattern) {
            when (part) {
                FilePart.DATETIME -> {
                    // DATETIME always has 2 tokens (date + time)
                    if (i + 1 < tokens.size) {
                        parts += tokens[i] + "_" + tokens[i + 1]
                        i += 2
                    } else {
                        parts += "" // fallback if malformed
                    }
                }
                else -> {
                    if (i < tokens.size) {
                        parts += tokens[i]
                        i++
                    } else {
                        parts += ""
                    }
                }
            }
        }
        return parts
    }


    fun fileNameFromPattern(
    ): String {

        val patternString= getString(context, KEY_FILENAME_PATTERN)
        val savedFileName= getString(context,KEY_FILE_NAME)

        if (patternString.isNullOrBlank() || savedFileName.isNullOrBlank()) {
            // return default name if no saved pattern or file
            return "${Date().formatForFile()}.jpg"
        }

        val pattern = patternString.split(",").map { FilePart.valueOf(it) }
//        val savedParts = savedFileName.removeSuffix(".jpg").split("_")
        val savedParts = parseSavedParts(pattern, savedFileName)

        // 🔹 Prepare dynamic values (only added if pattern contains them)
        val dynamicValues = mutableMapOf<FilePart, String>()

        if (pattern.contains(FilePart.DATETIME)) {
            dynamicValues[FilePart.DATETIME] =  if(getBoolean(context, KEY_24HOURS_CHECK)) Date().formatForFile(true)
            else Date().formatForFile()

        }

        if (pattern.contains(FilePart.DAY)) {
            dynamicValues[FilePart.DAY] = Date().getCurrentDay()
        }


        if (pattern.contains(FilePart.ADDRESS_LINE1)) {
            dynamicValues[FilePart.ADDRESS_LINE1] = address.line1
        }

        if (pattern.contains(FilePart.ADDRESS_LINE2)) {
            dynamicValues[FilePart.ADDRESS_LINE2] = address.line2
        }

        if (pattern.contains(FilePart.ADDRESS_LINE3)) {
            dynamicValues[FilePart.ADDRESS_LINE3] = address.line3
        }

        if (pattern.contains(FilePart.ADDRESS_LINE4)) {
            dynamicValues[FilePart.ADDRESS_LINE4] = address.line4
        }

        if (pattern.contains(FilePart.LATLONG)) {
            dynamicValues[FilePart.LATLONG] = if(getBoolean(context, KEY_DMS_CHECK)) "${latLongDMS.first},${latLongDMS.second}"
            else "${latLong.first},${latLong.second}"
        }

        if (pattern.contains(FilePart.PLUSCODE)) {
            dynamicValues[FilePart.PLUSCODE] = plusCode
        }

//        if (pattern.contains(FilePart.TIMEZONE)) {
//            dynamicValues[FilePart.TIMEZONE] = ""
//        }

        //  Build final list
        val finalParts = pattern.mapIndexed { index, part ->
            dynamicValues[part] ?: savedParts.getOrNull(index).orEmpty()
        }

        return finalParts.joinToString("_") + ".jpg"
    }
}