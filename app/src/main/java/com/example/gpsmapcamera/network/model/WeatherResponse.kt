package com.example.gpsmapcamera.network.model

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val name: String
)
