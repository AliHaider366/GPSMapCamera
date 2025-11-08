package com.example.gpsmapcamera.models

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val countryCode: String,
    var isSelected: Boolean = false
)