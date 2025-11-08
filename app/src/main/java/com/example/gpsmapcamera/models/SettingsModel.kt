package com.example.gpsmapcamera.models

sealed class SettingsModel{
    data class Heading(val heading: String) : SettingsModel()
    data class GeneralItem(val title: String,val icon: Int,val selectedOpt:String,val selectedOptIcon:Int) : SettingsModel()
    data class FeaturesItem(val title: String,val icon: Int) : SettingsModel()
    data class AboutItem(val title: String,val icon: Int) : SettingsModel()

}
