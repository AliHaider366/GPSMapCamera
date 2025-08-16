package com.example.gpsmapcamera.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.location.Location
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.gms.location.LocationServices
import com.google.openlocationcode.OpenLocationCode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume


fun Date.formatForFile(use24Hour: Boolean = false): String {
    val pattern = if (use24Hour) {
        "yyyyMMdd_HHmmss" // 24-hour format
    } else {
        "yyyyMMdd_hhmmssaa" // 12-hour format with AM/PM
    }
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    val result = formatter.format(this)
    return if (use24Hour) result else result.uppercase() // ensure AM/PM is uppercase
}

fun Date.getCurrentDay(short: Boolean = false): String {
    val pattern = if (short) "EEE" else "EEEE"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

fun Date.getGmtOffset(): String {
    val timeZone = TimeZone.getDefault()
    val offsetMillis = timeZone.rawOffset
    val hours = offsetMillis / (1000 * 60 * 60)
    val minutes = Math.abs((offsetMillis / (1000 * 60)) % 60)
    return String.format(Locale.getDefault(), "GMT%+03d:%02d", hours, minutes)
}
fun Double.toDMS(): String {
    val degrees = this.toInt()
    val minutesDecimal = (this - degrees) * 60
    val minutes = minutesDecimal.toInt()
    val seconds = ((minutesDecimal - minutes) * 60)
    return String.format("%d°%d'%s\"",
        degrees, minutes, String.format("%.2f", seconds))
}
fun Pair<Double, Double>.toDMSPair(): Pair<String, String> {
    fun Double.toDMS(): String {
        val degrees = this.toInt()
        val minutesDecimal = (this - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val seconds = (minutesDecimal - minutes) * 60
        return String.format("%d°%d'%.2f\"",
            degrees, minutes, seconds)
    }

    val latDMS = first.toDMS()
    val lonDMS = second.toDMS()
    return Pair(latDMS, lonDMS)
}
@Suppress("MissingPermission")
suspend fun Context.getCurrentLatLong(): Pair<Double, Double> =
    suspendCancellableCoroutine { cont ->
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                cont.resume(Pair(location.latitude, location.longitude))
            } else {
                cont.resume(Pair(0.0, 0.0))
            }
        }.addOnFailureListener {
            cont.resume(Pair(0.0, 0.0))
        }
    }

@Suppress("MissingPermission")
suspend fun Context.getCurrentPlusCode(): String =
    suspendCancellableCoroutine { cont ->
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                cont.resume(getPlusCode(location.latitude, location.longitude))
            } else {
                cont.resume("")
            }
        }.addOnFailureListener {
            cont.resume("")
        }
    }

fun getPlusCode(latitude: Double, longitude: Double): String {
    return OpenLocationCode.encode(latitude, longitude)
}

inline fun <reified T : Activity> Context.launchActivity(
    flags: Int = 0,
    noinline extras: (Intent.() -> Unit)? = null
) {
    val intent = Intent(this, T::class.java).apply {
        if (flags != 0) addFlags(flags)
        extras?.invoke(this)
    }
    startActivity(intent)
}

fun TextView.setDrawableEnd(drawableRes: Int?) {
    val drawable = drawableRes?.let { ContextCompat.getDrawable(context, it) }
    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

fun TextView.setTextColorRes(colorResId: Int) {
    this.setTextColor(ContextCompat.getColor(context, colorResId))
}
fun TextView.setTextColorRes(
    @ColorRes activeColor: Int,
    @ColorRes inactiveColor: Int? = null,
    vararg others: TextView?
) {
    // Set color for clicked TextView
    this.setTextColor(ContextCompat.getColor(context, activeColor))

    // Set color for all others
    if (inactiveColor != null) {
        others.forEach { tv ->
            tv?.setTextColor(ContextCompat.getColor(context, inactiveColor))
        }
    }
}
fun TextView.setTextColorAndDrawableTint(colorResId: Int) {
    val color = ContextCompat.getColor(context, colorResId)
    // Change text color
    this.setTextColor(color)
    // Change compound drawable tint
    TextViewCompat.setCompoundDrawableTintList(this, ContextCompat.getColorStateList(context, colorResId))
}

fun TextView.setTextColorAndBackgroundTint( textColorRes: Int, backgroundTintRes: Int) {
    // Change text color
    setTextColor(ContextCompat.getColor(context, textColorRes))
    // Change background tint
    ViewCompat.setBackgroundTintList(
        this,
        ColorStateList.valueOf(ContextCompat.getColor(context, backgroundTintRes))
    )
}

fun ImageView.setImage(@DrawableRes drawableRes: Int) {
    setImageResource(drawableRes)
}

@Suppress("DEPRECATION")
fun Activity.hideSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11+
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        // Below Android 11
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
}