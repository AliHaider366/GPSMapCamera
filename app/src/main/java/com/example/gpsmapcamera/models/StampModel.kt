package com.example.gpsmapcamera.models

import java.util.Date

//data class TemplateModel(var title: String = "", var icon: Int? =null)
data class StampConfig(
    val name: StampItemName,
    val visibility: Boolean = false,
    val position: StampPosition,
    val icon: Int? = null,
    val staticTitle: String? = null
)

enum class StampTemplate{
    ADVANCED, CLASSIC, REPORTING
}

data class FullAddress(
    var pinCode: String = "",
    var locality: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = ""
)

data class DynamicStampValues(
    val stampType : StampTemplate = StampTemplate.CLASSIC,
    val dateTime: Date?=null,
    val latLong: LatLon?=null,
    val shortAddress: String = "",
    val fullAddress: FullAddress = FullAddress(),
    val plusCode: String = "",
    val weather: Double = 0.0,
    val wind: Double = 0.0,
    val humidity: String = "",
    val pressure: Double = 0.0,
    val compass: String = "",
    val magneticField: String = "",
    val altitude: Double = 0.0,
    val accuracy: Double = 0.0,
    val soundLevel: String = ""
)

data class LatLon(val lat: Double, val lon: Double)


enum class StampPosition {
    BOTTOM, CENTER, RIGHT, NONE
}

enum class StampItemName {
    REPORTING_TAG,
    DATE_TIME,
    MAP_TYPE,
    SHORT_ADDRESS,
    FULL_ADDRESS,
    LAT_LONG,
    PLUS_CODE,
    TIME_ZONE,
    PERSON_NAME,
    CONTACT_NO,
    WEATHER,
    WIND,
    HUMIDITY,
    PRESSURE,
    COMPASS,
    MAGNETIC_FIELD,
    ALTITUDE,
    ACCURACY,
    SOUND_LEVEL,
    NUMBERING,
    LOGO,
    NOTE,
    STAMP_FONT,
    STAMP_SIZE,
    STAMP_POSITION,
    MAP_POSITION,
    MAP_SCALE
}