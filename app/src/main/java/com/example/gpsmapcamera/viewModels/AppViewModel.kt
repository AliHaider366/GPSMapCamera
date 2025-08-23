package com.example.gpsmapcamera.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val fileSavePath=SAVED_DEFAULT_FILE_PATH
}