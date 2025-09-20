package com.example.gpsmapcamera.models

data class FieldItem(
    val index:Int,
    val name: String,
    var value: String,
    val day: String="",
    val address: AddressLineModel?=null,
    val latLongDMS: String="",
    var isChecked: Boolean = false,
    var isCheckBox1Checked: Boolean = false,
    var isCheckBox2Checked: Boolean = false,
    var isCheckBox3Checked: Boolean = false,
    var isCheckBox4Checked: Boolean = false,
    var isDropCheck: Boolean?=null,
    var isDropDownVisible: Boolean?=null,
    var isPremium: Boolean=false
)