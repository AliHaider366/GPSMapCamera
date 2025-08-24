package com.example.gpsmapcamera.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH
import com.example.gpsmapcamera.utils.Constants.SAVED_FILE_NAME
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.getFileName
import com.example.gpsmapcamera.utils.PrefManager.getFilePath
import com.example.gpsmapcamera.utils.PrefManager.saveFileName
import com.example.gpsmapcamera.utils.PrefManager.saveFilePath
import com.example.gpsmapcamera.utils.formatForFile
import java.util.Date

class AppViewModel( application: Application) : AndroidViewModel(application) {

    var fileSavePath=SAVED_DEFAULT_FILE_PATH
        private set
    var saveFileName= SAVED_FILE_NAME
        private set

    private val context = getApplication<Application>()


    fun setFileName(name: String) {
        saveFileName = name
        saveFileName(context,name)       ///save file name in pref
    }
    fun setFileSavedPath(path: String) {
        fileSavePath = path
        saveFilePath(context,path)       /// save file path in pref
    }

    init {
        saveFileName= getFileName(context)
        fileSavePath= getFilePath(context)
    }
}