package com.example.gpsmapcamera.viewModels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gpsmapcamera.enums.FilePart
import com.example.gpsmapcamera.models.AddressLineModel
import com.example.gpsmapcamera.models.AddressModel
import com.example.gpsmapcamera.models.DynamicStampValues
import com.example.gpsmapcamera.models.FullAddress
import com.example.gpsmapcamera.models.LatLon
import com.example.gpsmapcamera.models.StampConfig
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.models.StampPosition
import com.example.gpsmapcamera.network.api.WeatherApiService
import com.example.gpsmapcamera.network.model.WeatherResponse
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.Constants.SAVED_DEFAULT_FILE_PATH
import com.example.gpsmapcamera.utils.Constants.SAVED_FILE_NAME
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.PrefManager.KEY_24HOURS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_DMS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILENAME_PATTERN
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILE_NAME
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILE_PATH
import com.example.gpsmapcamera.utils.PrefManager.KEY_FOLDER_NAME
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.formatForFile
import com.example.gpsmapcamera.utils.formatPlusCodeByPosition
import com.example.gpsmapcamera.utils.getCurrentAddress
import com.example.gpsmapcamera.utils.getCurrentDay
import com.example.gpsmapcamera.utils.getCurrentLatLong
import com.example.gpsmapcamera.utils.getCurrentPlusCode
import com.example.gpsmapcamera.utils.getIcon
import com.example.gpsmapcamera.utils.isMicrophonePermissionGranted
import com.example.gpsmapcamera.utils.toDMSPair
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.sqrt

class AppViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    var fileSavePath = SAVED_DEFAULT_FILE_PATH
        private set
    var saveFileName = SAVED_FILE_NAME
        private set


    var plusCode = ""
        private set

    var latLong = 0.0 to 0.0
        private set
    var latLongDMS = "0" to "0"
        private set

    var address = AddressLineModel()
        private set


    fun getLocationFile() {
        viewModelScope.launch {
            plusCode = context.getCurrentPlusCode()
            address = context.getCurrentAddress()
            latLong = context.getCurrentLatLong()
            latLongDMS = latLong.toDMSPair()
        }
    }

    fun setFileName(name: String) {
        saveFileName = name
        PrefManager.saveString(context, KEY_FILE_NAME, name)       ///save file name in pref
    }

    fun setFileSavedPath(path: String, folderName: String) {
        fileSavePath = path
        PrefManager.saveString(context, KEY_FILE_PATH, path)      /// save file path in pref
        PrefManager.saveString(context, KEY_FOLDER_NAME, folderName)    /// save folder name in pref
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

        val patternString = PrefManager.getString(context, KEY_FILENAME_PATTERN)
        val savedFileName = PrefManager.getString(context, KEY_FILE_NAME)

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
            dynamicValues[FilePart.DATETIME] =
                if (PrefManager.getBoolean(context, KEY_24HOURS_CHECK)) Date().formatForFile(true)
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
            dynamicValues[FilePart.LATLONG] = if (PrefManager.getBoolean(
                    context,
                    KEY_DMS_CHECK
                )
            ) "${latLongDMS.first},${latLongDMS.second}"
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


    // Separate LiveData for each template
    private val _classicStampConfigs = MutableLiveData<List<StampConfig>>()
    val classicStampConfigs: LiveData<List<StampConfig>> get() = _classicStampConfigs

    private val _advanceStampConfigs = MutableLiveData<List<StampConfig>>()
    val advanceStampConfigs: LiveData<List<StampConfig>> get() = _advanceStampConfigs

    private val _reportingStampConfigs = MutableLiveData<List<StampConfig>>()
    val reportingStampConfigs: LiveData<List<StampConfig>> get() = _reportingStampConfigs


    // ... (other existing fields and sensor/location logic remain unchanged)


    private fun loadConfigsForTemplate(
        templateType: String,
        liveData: MutableLiveData<List<StampConfig>>
    ) {
        val saved = prefs.getList(templateType)
        if (saved.isNotEmpty()) {
            liveData.value = saved
        } else {
            val defaults = getDefaultConfigs()
            liveData.value = defaults
            prefs.saveList(templateType, defaults)
        }
    }

    fun updateStampVisibility(templateType: String, itemName: StampItemName, isChecked: Boolean) {
        val liveData = when (templateType) {
            Constants.CLASSIC_TEMPLATE -> _classicStampConfigs
            Constants.ADVANCE_TEMPLATE -> _advanceStampConfigs
            Constants.REPORTING_TEMPLATE -> _reportingStampConfigs
            else -> return
        }

        val currentList = liveData.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.name == itemName }
        if (index != -1) {
            currentList[index] = currentList[index].copy(visibility = isChecked)
            liveData.value = currentList
            prefs.saveList(templateType, currentList)
        }
    }

    fun updateStaticTitle(templateType: String, itemName: StampItemName, newTitle: String) {
        val liveData = when (templateType) {
            Constants.CLASSIC_TEMPLATE -> _classicStampConfigs
            Constants.ADVANCE_TEMPLATE -> _advanceStampConfigs
            Constants.REPORTING_TEMPLATE -> _reportingStampConfigs
            else -> return
        }

        val currentList = liveData.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.name == itemName }
        if (index != -1) {
            currentList[index] = currentList[index].copy(staticTitle = newTitle)
            liveData.value = currentList
            prefs.saveList(templateType, currentList)
        }
    }


    private val prefs = StampPreferences(application)
    private val _stampConfigs = MutableLiveData<List<StampConfig>>()
    val stampConfigs: LiveData<List<StampConfig>> get() = _stampConfigs

    private val _dynamicValues = MutableLiveData<DynamicStampValues>()
    val dynamicValues: LiveData<DynamicStampValues> get() = _dynamicValues

    // Sensor and location services
    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private val geocoder = Geocoder(application, Locale.ENGLISH)

    // Weather API
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val weatherApiService: WeatherApiService =
        retrofit.create(WeatherApiService::class.java)
    private val apiKey =
        "1e6dda0b3ca6da0b57807888078495b9" // Replace with your OpenWeatherMap API key

    // MediaRecorder for sound level
    private var mediaRecorder: MediaRecorder? = null
    private var recordingJob: Job? = null

    // Sensor data
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private var currentAzimuthDegrees: Double = 0.0
    private var currentMagneticStrength: Double = 0.0
    private var currentSoundLevel: Double = 0.0
    private var currentLocation: android.location.Location? = null
    private var currentWeather: WeatherResponse? = null // Assume WeatherResponse is your weather API model

    // Coroutine for throttling updates
    private var updateJob: Job? = null


    private val context = getApplication<Application>()


    init {
        // Load configs for each template
        loadConfigsForTemplate(Constants.CLASSIC_TEMPLATE, _classicStampConfigs)
        loadConfigsForTemplate(Constants.ADVANCE_TEMPLATE, _advanceStampConfigs)
        loadConfigsForTemplate(Constants.REPORTING_TEMPLATE, _reportingStampConfigs)


        saveFileName = PrefManager.getString(context, KEY_FILE_NAME, SAVED_FILE_NAME)
        fileSavePath = PrefManager.getString(context, KEY_FILE_PATH, SAVED_DEFAULT_FILE_PATH)


        // Start dynamic data collection
        startDynamicUpdates()
        getLocationFile()

    }

    fun getLocation(): Location? {
        return currentLocation
    }

    fun updateLocation(location : Location)  {
        Log.d("TAG", "updateLocation: longitude ${location.longitude} latitude 9${location.latitude}")
        currentLocation = location
    }

    private var locationCallback: ((Location) -> Unit)? = null


    fun getLocationAndFetch(callback: (Location?) -> Unit) {
        if (currentLocation != null) {
            callback(currentLocation!!)
        } else {
            locationCallback = callback
        }
    }


    private fun getDefaultConfigs(): List<StampConfig> {
        return listOf(
            StampConfig(
                StampItemName.CONTACT_NO,
                false,
                StampPosition.BOTTOM,
                StampItemName.CONTACT_NO.getIcon(),
                PrefManager.getString(context, Constants.ADDED_PHONE_NUMBER)
            ),
            StampConfig(
                StampItemName.NOTE,
                false,
                StampPosition.CENTER,
                null,
                "Note: Captured by GPS Map Camera"
            ),
            StampConfig(
                StampItemName.PERSON_NAME,
                false,
                StampPosition.CENTER,
                null,
                "Person name:"
            ),
            StampConfig(StampItemName.NUMBERING, false, StampPosition.CENTER, null, "1"),
            StampConfig(
                StampItemName.SOUND_LEVEL,
                false,
                StampPosition.BOTTOM,
                StampItemName.SOUND_LEVEL.getIcon()
            ),
            StampConfig(
                StampItemName.ALTITUDE,
                false,
                StampPosition.BOTTOM,
                StampItemName.ALTITUDE.getIcon()
            ),
            StampConfig(
                StampItemName.ACCURACY,
                false,
                StampPosition.BOTTOM,
                StampItemName.ACCURACY.getIcon()
            ),
            StampConfig(StampItemName.SHORT_ADDRESS, true, StampPosition.CENTER),
            StampConfig(StampItemName.FULL_ADDRESS, true, StampPosition.CENTER),
            StampConfig(StampItemName.LAT_LONG, true, StampPosition.CENTER),
            StampConfig(StampItemName.DATE_TIME, true, StampPosition.CENTER),
            StampConfig(StampItemName.TIME_ZONE, false, StampPosition.CENTER),
            StampConfig(StampItemName.PLUS_CODE, false, StampPosition.CENTER),
            StampConfig(
                StampItemName.WEATHER,
                false,
                StampPosition.RIGHT,
                StampItemName.WEATHER.getIcon()
            ),
            StampConfig(
                StampItemName.COMPASS,
                false,
                StampPosition.RIGHT,
                StampItemName.COMPASS.getIcon()
            ),
            StampConfig(
                StampItemName.MAGNETIC_FIELD,
                false,
                StampPosition.RIGHT,
                StampItemName.MAGNETIC_FIELD.getIcon()
            ),
            StampConfig(
                StampItemName.PRESSURE,
                false,
                StampPosition.RIGHT,
                StampItemName.PRESSURE.getIcon()
            ),
            StampConfig(
                StampItemName.WIND,
                false,
                StampPosition.RIGHT,
                StampItemName.WIND.getIcon()
            ),
            StampConfig(
                StampItemName.HUMIDITY,
                false,
                StampPosition.RIGHT,
                StampItemName.HUMIDITY.getIcon()
            ),
            StampConfig(StampItemName.MAP_TYPE, true, StampPosition.NONE),
            StampConfig(StampItemName.REPORTING_TAG, false, StampPosition.NONE),
            StampConfig(StampItemName.LOGO, false, StampPosition.NONE)
        )
    }


    private fun startDynamicUpdates() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }

        // Start sensor listeners
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start sound recording
        startSoundRecording()

        // Start throttled updates
        updateJob = viewModelScope.launch {
            while (isActive) {
                updateDynamicValues()
                delay(1000L) // Throttle to 1Hz; adjust as needed
            }
        }
    }

    private fun startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            viewModelScope.launch {


                val (lat, lon) = context.getCurrentLatLong()

                currentLocation = android.location.Location("provider").apply {
                    latitude = lat
                    longitude = lon
                }

                currentLocation?.let { it ->

                    locationCallback?.invoke(it) // notify waiting getLocation()
                    locationCallback = null
                    fetchWeatherData(it.latitude, it.longitude)
                }


            }


            /*            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            currentLocation = location
                            currentLocation?.let { it ->

                                locationCallback?.invoke(location) // notify waiting getLocation()
                                locationCallback = null
                                fetchWeatherData(it.latitude, it.longitude)
                            }
                        }*/
        }
    }

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                currentWeather = weatherApiService.getCurrentWeather(latitude, longitude, apiKey)
            } catch (e: Exception) {
                // Log error; optionally post empty weather values
            }
        }
    }

    private fun startSoundRecording() {
        if (context.isMicrophonePermissionGranted()) {
            val outputFile =
                File.createTempFile("temp_audio", ".3gp", getApplication<Application>().cacheDir)
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setOutputFile(outputFile)
                } else {
                    setOutputFile("/dev/null")
                }
                try {
                    prepare()
                    start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            recordingJob = viewModelScope.launch {
                while (isActive) {
                    val amplitude = mediaRecorder?.maxAmplitude ?: 0
                    currentSoundLevel = if (amplitude > 0) 20 * log10(amplitude.toDouble()) else 0.0
                    delay(500L)
                }
            }
        } else {
            currentSoundLevel = 0.0
        }
    }

    private fun stopSoundRecording() {
        recordingJob?.cancel()
        mediaRecorder?.apply {
            try {
                stop()
                reset()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerReading = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetometerReading = event.values.clone()
        }

        val rotationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        if (success) {
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            currentAzimuthDegrees = (Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360
            currentMagneticStrength = sqrt(
                (magnetometerReading[0] * magnetometerReading[0] +
                        magnetometerReading[1] * magnetometerReading[1] +
                        magnetometerReading[2] * magnetometerReading[2]).toDouble()
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    private fun updateDynamicValues() {

        val location = currentLocation

        var fullAddressModel = FullAddress()
        var shortAddress = ""
        var plusCode = ""
        if (location != null) {
            try {
                plusCode = "Plus Code: ${
                    LatLon(
                        location.latitude,
                        location.longitude
                    ).formatPlusCodeByPosition(0)
                }"
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    shortAddress = "${address.locality}, ${address.countryName}"


                    // ✅ Add each part only if you want to show it

                    //Plus code / pin code
                    if (/*showFeatureName &&*/ !address.featureName.isNullOrEmpty()) {
                        fullAddressModel.pinCode = address.featureName
//                        Log.d("TAG", "updateDynamicValues:  featureName ${address.featureName}")
                    }

                    address.getAddressLine(0)?.let {
                        fullAddressModel.locality = it
                    } ?: run {
                        if (/*showCity && */!address.subAdminArea.isNullOrEmpty()) {
//                        Log.d("TAG", "updateDynamicValues:  locality ${address.locality}")
                            fullAddressModel.locality = address.subAdminArea
                        }
                    }

                    if (/*showProvince && */!address.locality.isNullOrEmpty()) {
//                        Log.d("TAG", "updateDynamicValues:  subAdminArea ${address.subAdminArea}")
                        fullAddressModel.city = address.locality
                    }

                    if (/*showState &&*/ !address.adminArea.isNullOrEmpty()) {
//                        Log.d("TAG", "updateDynamicValues:  adminArea ${address.adminArea}")
                        fullAddressModel.state = address.adminArea

                    }

                    if (/*showCountry &&*/ !address.countryName.isNullOrEmpty()) {
//                        Log.d("TAG", "updateDynamicValues:  countryName ${address.countryName}")
                        fullAddressModel.country = address.countryName
                    }

                }

            } catch (e: Exception) {

            }
        }

//        val fullAddress2 = if (location != null) {
//            try {
//                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
//                addresses?.firstOrNull()?.getAddressLine(0) ?: ""
//            } catch (e: Exception) {
//                ""
//            }
//        } else {
//            ""
//        }
//        Log.d("TAG", "updateDynamicValues:  fullAddress2 ${fullAddress2}")
//


        _dynamicValues.postValue(
            DynamicStampValues(
                dateTime = Date(),
                latLong = if (location != null) LatLon(
                    location.latitude,
                    location.longitude
                ) else LatLon(0.0, 0.0),
                shortAddress = shortAddress,
                fullAddress = fullAddressModel,
                plusCode = plusCode,
                weather = currentWeather?.main?.temp ?: 0.0,
                wind = currentWeather?.wind?.speed ?: 0.0,
                humidity = currentWeather?.let
                { "${it.main.humidity}%" } ?: "",
                pressure = currentWeather?.main?.pressure?.toDouble() ?: 0.0,
                compass = "${currentAzimuthDegrees.toInt()}° (${
                    getDirectionFromAngle(
                        currentAzimuthDegrees
                    )
                })",
                magneticField = "%.2f µT".format(currentMagneticStrength),
                altitude = currentLocation?.altitude ?: 0.0,
                accuracy = currentLocation?.accuracy?.toDouble() ?: 0.0,
//
//                altitude = currentLocation?.let
//                { "%.2f m".format(it.altitude) } ?: "",
//                accuracy = currentLocation?.let
//                { "%.2f m".format(it.accuracy) } ?: "",
                soundLevel = "%.2f dB".format(currentSoundLevel)
            )
        )
    }

    private fun getDirectionFromAngle(angle: Double): String {
        return when (angle) {
            in 337.5..360.0, in 0.0..22.5 -> "N"
            in 22.5..67.5 -> "NE"
            in 67.5..112.5 -> "E"
            in 112.5..157.5 -> "SE"
            in 157.5..202.5 -> "S"
            in 202.5..247.5 -> "SW"
            in 247.5..292.5 -> "W"
            in 292.5..337.5 -> "NW"
            else -> "?"
        }
    }

    fun getEffectiveTitle(config: StampConfig, dynamics: DynamicStampValues): String {
        return config.staticTitle ?: when (config.name) {
            StampItemName.SHORT_ADDRESS -> dynamics.shortAddress
            StampItemName.PLUS_CODE -> dynamics.plusCode
            StampItemName.HUMIDITY -> dynamics.humidity
            StampItemName.COMPASS -> dynamics.compass
            StampItemName.MAGNETIC_FIELD -> dynamics.magneticField
            StampItemName.SOUND_LEVEL -> dynamics.soundLevel
            else -> ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        updateJob?.cancel()
        stopSoundRecording()
        fusedLocationClient.removeLocationUpdates {}
    }
}