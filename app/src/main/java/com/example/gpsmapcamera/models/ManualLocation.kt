package com.example.gpsmapcamera.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ManualLocation(
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val range: Int = 50
) : Parcelable

