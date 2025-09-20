package com.example.gpsmapcamera.models

data class MapTypeModel(
    var title: String = "",
    var icon: Int = 0,
    var mapType: MapType = MapType.NORMAL
)

enum class MapType {
    NORMAL, SATELLITE, TERRAIN, HYBRID
}