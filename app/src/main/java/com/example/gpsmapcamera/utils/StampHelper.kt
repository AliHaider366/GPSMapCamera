package com.example.gpsmapcamera.utils

import android.content.Context
import android.transition.TransitionManager
import android.util.Log
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.StampAdvanceTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampClassicTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampReportingTemplateLayoutBinding
import com.example.gpsmapcamera.models.LatLon
import com.example.gpsmapcamera.models.NoteModel
import com.example.gpsmapcamera.models.StampCameraPosition
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.google.openlocationcode.OpenLocationCode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

val dateFormats = arrayListOf(
    "MM/dd/yyyy hh:mm a",
    "dd/MM/yyyy HH:mm",
    "yyyy-MM-dd HH:mm:ss",
    "dd-MMM-yyyy hh:mm a",
    "MMMM dd, yyyy hh:mm a",
    "dd/MM/yyyy hh:mm a",
    "yyyy-MM-dd hh:mm a",
    "MM-dd-yyyy HH:mm",
    "dd-MMM-yyyy HH:mm:ss",
    "MMM dd, yyyy hh:mm a",
    "yyyy/MM/dd HH:mm:ss"
)

val timeZoneFormats = arrayListOf(
    "+0500",
    "UTC +0500",
    "GMT +0500",
    "UTC +05:00",
    "GMT +05:00",
    "+05:00",
    "Pakistan Standard Time"
)

val reportingTagsDefault = arrayListOf(
    "Environment",
    "Check In",
    "Monitoring",
    "Installation",
    "Unlock",
    "Infrastructure",
    "Clock In",
    "Clock Out",
)

val temperatureFormats = arrayListOf(
    "C",
    "F",
)


val windFormats = arrayListOf(
    "KM/h",
    "mph",
    "m/s",
    "kt",
)


val pressureFormats = arrayListOf(
    "hpa",
    "mmhp",
    "inHq",
)


val altitudeAccuracyFormats = arrayListOf(
    "m",
    "ft",
)


val recentNotesDefault = arrayListOf<NoteModel>(
    NoteModel("Captured by GPS Map Camera", "Note:")
)

val coordinateFormats = arrayListOf(
    "Decimal",
    "Dec Degs",
    "Dec Degs Micro",
    "Dec Mins",
    "Dec Mins Secs",
    "UTM",
    "MGRS / USNG"
)


val plusCodeFormats = arrayListOf(
    "Accurate",
    "Concise",
)


val stampFontList by lazy {
    arrayListOf<Int?>(
        null,
        R.font.short_stack,
        R.font.skranji,
        R.font.single_day,
        R.font.squada_one,
        R.font.source_serif_pro_semibold,
        R.font.stylish
    )
}

fun LatLon.formatLatLong(context: Context, template: String): String {
    val selectedLatLongIndex = getInt(context, Constants.SELECTED_LAT_LONG + template, 0)
    Log.d("TAG", "formatLatLong: $template")
    return try {
        formatCoordinatesByPosition(selectedLatLongIndex)
    } catch (e: Exception) {
        "N/A"
    }
}

fun LatLon.formatPlusCode(context: Context, template: String): String {
    val selectedLatLongIndex = getInt(context, Constants.SELECTED_PLUS_CODE + template, 0)
    Log.d("TAG", "formatPlusCode: $template")
    return try {
        formatPlusCodeByPosition(selectedLatLongIndex)
    } catch (e: Exception) {
        "N/A"
    }
}

fun Date.formatDate(context: Context, template: String): String {
    val dateFormatIndex = getInt(context, Constants.DATE_TIME_SELECTED_FORMAT + template, 0)
    Log.d("TAG", "formatDateTemplate: $template")

    val timeZoneId = TimeZone.getDefault().id

    val dateFormatter = SimpleDateFormat(dateFormats[dateFormatIndex], Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(timeZoneId)
    }

    return try {
        dateFormatter.format(this)
    } catch (e: Exception) {
        "N/A"
    }
}

fun Date.formatTimeZone(context: Context, template: String): String {
    val timeZoneFormatIndex = getInt(context, Constants.TIME_ZONE_SELECTED_FORMAT + template, 0)
    val timeZoneId = TimeZone.getDefault().id

    return when (timeZoneFormats[timeZoneFormatIndex]) {
        "+0500" -> {
            SimpleDateFormat("Z", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
                .format(this)
        }

        "UTC +0500" -> {
            "UTC " + SimpleDateFormat("Z", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
                .format(this)
        }

        "GMT +0500" -> {
            "GMT " + SimpleDateFormat("Z", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
                .format(this)
        }

        "UTC +05:00" -> {
            "UTC " + SimpleDateFormat("XXX", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
                .format(this)
        }

        "GMT +05:00" -> {
            "GMT " + SimpleDateFormat("XXX", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
                .format(this)
        }

        "+05:00" -> {
            SimpleDateFormat("XXX", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(timeZoneId)
            }
                .format(this)
        }

        "Pakistan Standard Time" -> {
            TimeZone.getTimeZone(timeZoneId).getDisplayName(false, TimeZone.LONG, Locale.ENGLISH)
        }

        else -> "N/A"
    }

}


// -------------------------
// Extensions for LatLon
// -------------------------


fun LatLon.formatCoordinatesByPosition(position: Int): String {
    return when (position) {
        0 -> toDecimal()
        1 -> toDecDegs()
        2 -> toDecDegsMicro()
        3 -> toDecMins()
        4 -> toDMS()
        5 -> toUTM()
        6 -> toMGRS()
        else -> toDecimal()
    }
}

fun LatLon.formatPlusCodeByPosition(position: Int): String {
    return when (position) {
        0 -> toAccuratePlusCode()
        1 -> toConcisePlusCode()
        else -> toAccuratePlusCode()
    }
}

// 1. Decimal
fun LatLon.toDecimal(): String =
    "Lat $lat Lon $lon"

// 2. Decimal Degrees (6 decimals)
fun LatLon.toDecDegs(): String =
    "Lat %.6f Lon %.6f".format(lat, lon)

// 3. Decimal Degrees Micro (8 decimals)
fun LatLon.toDecDegsMicro(): String =
    "Lat %.8f Lon %.8f".format(lat, lon)

// 4. Decimal Minutes
fun LatLon.toDecMins(): String {
    fun convert(value: Double): String {
        val degrees = floor(value)
        val minutes = (value - degrees) * 60
        return "${degrees.toInt()}° %.5f'".format(minutes)
    }
    return "Lat ${convert(lat)} Lon ${convert(lon)}"
}

// 5. Degrees Minutes Seconds (DMS)
fun LatLon.toDMS(): String {
    fun convert(value: Double): String {
        val degrees = floor(value)
        val minutesFull = (value - degrees) * 60
        val minutes = floor(minutesFull)
        val seconds = (minutesFull - minutes) * 60
        return "${degrees.toInt()}° ${minutes.toInt()}' %.2f\"".format(seconds)
    }
    return "Lat ${convert(lat)} Lon ${convert(lon)}"
}

// -------------------------
// UTM Conversion
// -------------------------
fun LatLon.toUTM(): String {
    val a = 6378137.0            // WGS84 major axis
    val f = 1 / 298.257223563    // WGS84 flattening
    val k0 = 0.9996              // scale factor

    val e = sqrt(f * (2 - f))    // eccentricity
    val zone = ((lon + 180) / 6).toInt() + 1

    val λ0 = Math.toRadians(((zone - 1) * 6 - 180 + 3).toDouble()) // central meridian
    val φ = Math.toRadians(lat)
    val λ = Math.toRadians(lon)

    val N = a / sqrt(1 - e * e * sin(φ).pow(2))
    val T = tan(φ).pow(2)
    val C = (e * e / (1 - e * e)) * cos(φ).pow(2)
    val A = cos(φ) * (λ - λ0)

    val M = a * ((1 - e * e / 4 - 3 * e.pow(4) / 64 - 5 * e.pow(6) / 256) * φ
            - (3 * e * e / 8 + 3 * e.pow(4) / 32 + 45 * e.pow(6) / 1024) * sin(2 * φ)
            + (15 * e.pow(4) / 256 + 45 * e.pow(6) / 1024) * sin(4 * φ)
            - (35 * e.pow(6) / 3072) * sin(6 * φ))

    val easting = (k0 * N * (A + (1 - T + C) * A.pow(3) / 6
            + (5 - 18 * T + T * T + 72 * C - 58 * (e * e / (1 - e * e))) * A.pow(5) / 120)
            + 500000.0)

    var northing = (k0 * (M + N * tan(φ) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A.pow(4) / 24
            + (61 - 58 * T + T * T + 600 * C - 330 * (e * e / (1 - e * e))) * A.pow(6) / 720)))

    val hemisphere = if (lat >= 0) "N" else "S"
    if (lat < 0) northing += 10000000.0

    // Latitude band letters (C-X, skipping I & O)
    val bands = "CDEFGHJKLMNPQRSTUVWX"
    val bandIndex = ((lat + 80) / 8).toInt().coerceIn(0, bands.length - 1)
    val band = bands[bandIndex]

    return "%d%s %s %.1f %.1f".format(zone, band, hemisphere, easting, northing)
}

// -------------------------
// MGRS Conversion
// -------------------------
fun LatLon.toMGRS(): String {
    val utm = this.toUTM()

    // Extract easting/northing from UTM string
    val regex = """(\d+)([C-X]) ([NS]) ([0-9.]+) ([0-9.]+)""".toRegex()
    val match = regex.find(utm) ?: return "MGRS conversion failed"

    val zone = match.groupValues[1]
    val band = match.groupValues[2]
    val easting = match.groupValues[4].toDouble()
    val northing = match.groupValues[5].toDouble()

    // 100km grid column letters (simplified)
    val columns = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val rows = "ABCDEFGHJKLMNPQRSTUV"

    val col = columns[(floor(easting / 100000).toInt()) % columns.length]
    val row = rows[(floor(northing / 100000).toInt()) % rows.length]

    // Trimmed 5-digit Easting/Northing
    val e = (easting % 100000).toInt()
    val n = (northing % 100000).toInt()

    return "$zone$band$col$row %05d %05d".format(e, n)
}


// Accurate / full Plus Code
fun LatLon.toAccuratePlusCode(): String {
    return "Plus Code:  ${OpenLocationCode.encode(lat, lon)}"
}

// Concise / short Plus Code (manual implementation)
fun LatLon.toConcisePlusCode(): String {
    val full = OpenLocationCode.encode(lat, lon)

    // Chop off 4 characters before the '+', like Google does locally
    val plusIndex = full.indexOf('+')
    return if (plusIndex > 4) {
        "Plus Code:  ${full.substring(plusIndex - 4)}" // local-looking short code
    } else {
        "Plus Code:  $full"
    }
}

fun Double.toTemperature(context: Context, passedTemplate: String): String {
    val selectedTemperatureIndex =
        getInt(context, Constants.FROM_TEMPERATURE_MODULE + passedTemplate, 0)
    return when (selectedTemperatureIndex) {
        1 -> "${((this * 9 / 5) + 32).toInt()}°F"   // Celsius → Fahrenheit
        else -> "${this}°C"
    }
}

fun Double.toWindSpeed(context: Context, passedTemplate: String): String {
    val selectedWindIndex = getInt(context, Constants.FROM_WIND_MODULE + passedTemplate, 0)
    return when (selectedWindIndex) {
        0 -> "${this} km/h"                          // already in km/h
        1 -> "${(this / 1.609).toInt()} mph"         // km/h → mph
        2 -> "${(this / 3.6).toInt()} m/s"           // km/h → m/s
        3 -> "${(this / 1.852).toInt()} kt"          // km/h → knots
        else -> "${this} km/h"
    }
}


fun Double.toPressure(context: Context, passedTemplate: String): String {
    val selectedPressureIndex = getInt(context, Constants.FROM_PRESSURE_MODULE + passedTemplate, 0)
    return when (selectedPressureIndex) {
        0 -> "${this.toInt()} hPa"                         // already hPa
        1 -> "${(this * 0.75006).toInt()} mmHg"            // hPa → mmHg
        2 -> "${(this * 0.02953).toInt()} inHg"            // hPa → inHg
        else -> "${this.toInt()} hPa"
    }
}

fun Double.toAltitude(context: Context, passedTemplate: String): String {
    val selectedAltitudeIndex = getInt(context, Constants.FROM_ALTITUDE + passedTemplate, 0)
    return when (selectedAltitudeIndex) {
        0 -> "${this.toInt()} m"                      // meters
        1 -> "${(this * 3.28084).toInt()} ft"         // meters → feet
        else -> "${this.toInt()} m"
    }
}

fun Double.toAccuracy(context: Context, passedTemplate: String): String {
    val selectedAccuracyIndex = getInt(context, Constants.FROM_ACCURACY + passedTemplate, 0)
    return when (selectedAccuracyIndex) {
        0 -> "${this.toInt()} m"                      // meters
        1 -> "${(this * 3.28084).toInt()} ft"         // meters → feet
        else -> "${this.toInt()} m"
    }
}


fun Context.setUpMapPositionForAdvancedTemplate(binding: StampAdvanceTemplateLayoutBinding) {
    binding.apply {
        val constraintSet = ConstraintSet()
        constraintSet.clone(root)

        // Define margins (using your SDP resources)
        val sideMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)
        val sideMarginForTitle = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp)
        val sideMargin10sdp = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        val logoMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._20sdp)

        val mapPosition = PrefManager.getInt(
            this@setUpMapPositionForAdvancedTemplate,
            Constants.SELECTED_MAP_POSITION + Constants.ADVANCE_TEMPLATE,
            0
        )

        // Clear all relevant constraints first to avoid undefined references
        constraintSet.clear(mapContainer.id, ConstraintSet.START)
        constraintSet.clear(mapContainer.id, ConstraintSet.END)
        constraintSet.clear(rvCenter.id, ConstraintSet.START)
        constraintSet.clear(rvCenter.id, ConstraintSet.END)
        constraintSet.clear(rvRight.id, ConstraintSet.START)
        constraintSet.clear(rvRight.id, ConstraintSet.END)
        constraintSet.clear(tvCenterTitle.id, ConstraintSet.START)
        constraintSet.clear(ivLogo.id, ConstraintSet.END)

        if (mapPosition == 1) {
            // Set mapContainer to the far right
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.constrainPercentWidth(mapContainer.id, 0.3f)

            // Set rvRight to the left of mapContainer
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvRight.id, ConstraintSet.WRAP_CONTENT)

            // Set rvCenter to the left of rvRight, filling remaining space
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.END,
                rvRight.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvCenter.id, ConstraintSet.MATCH_CONSTRAINT)

            // Align tvCenterTitle with rvCenter
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.START,
                sideMarginForTitle
            )

            // Position ivLogo to the left of mapContainer to avoid overlap
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                logoMargin
            )
        } else {
            // Restore mapContainer to the left
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.END,
                rvCenter.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainPercentWidth(mapContainer.id, 0.3f)

            // Set rvCenter to the right of mapContainer
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.END,
                rvRight.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvCenter.id, ConstraintSet.MATCH_CONSTRAINT)

            // Set rvRight to the right of rvCenter
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.constrainWidth(rvRight.id, ConstraintSet.WRAP_CONTENT)

            // Align tvCenterTitle with mapContainer
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin10sdp
            )

            // Restore ivLogo to original position
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
        }

        // Add smooth transition animation
        TransitionManager.beginDelayedTransition(root)

        // Apply the new constraints
        constraintSet.applyTo(root)
    }
}

fun Context.setUpMapPositionForClassicTemplate(binding: StampClassicTemplateLayoutBinding) {
    binding.apply {
        val constraintSet = ConstraintSet()
        constraintSet.clone(root)

        // Define margins (using your SDP resources)
        val sideMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)
        val logoMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._20sdp)
        val titleTopMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp)
        val sideMargin10sdp = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        val bottomMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp)

        val mapPosition = PrefManager.getInt(
            this@setUpMapPositionForClassicTemplate,
            Constants.SELECTED_MAP_POSITION + Constants.CLASSIC_TEMPLATE,
            0
        )

        // Clear all relevant constraints first to avoid undefined references
        constraintSet.clear(mapContainer.id, ConstraintSet.START)
        constraintSet.clear(mapContainer.id, ConstraintSet.END)
        constraintSet.clear(rvCenter.id, ConstraintSet.START)
        constraintSet.clear(rvCenter.id, ConstraintSet.END)
        constraintSet.clear(rvRight.id, ConstraintSet.START)
        constraintSet.clear(rvRight.id, ConstraintSet.END)
        constraintSet.clear(tvCenterTitle.id, ConstraintSet.START)
        constraintSet.clear(ivLogo.id, ConstraintSet.END)
        constraintSet.clear(rvBottom.id, ConstraintSet.START)
        constraintSet.clear(rvBottom.id, ConstraintSet.END)

        if (mapPosition == 1) {
            // Set mapContainer to the far right
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                0
            )
            constraintSet.constrainPercentWidth(mapContainer.id, 0.3f)

            // Set rvRight to the left of mapContainer
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.constrainWidth(rvRight.id, ConstraintSet.WRAP_CONTENT)

            // Set rvCenter to the left of rvRight, filling remaining space
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.END,
                rvRight.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvCenter.id, ConstraintSet.MATCH_CONSTRAINT)

            // Align tvCenterTitle with rvCenter
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.START,
                titleTopMargin
            )
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                titleTopMargin
            )

            // Position ivLogo to the left of mapContainer to avoid overlap
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                logoMargin
            )
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0
            )

            // Set rvBottom to the left of mapContainer
            constraintSet.connect(
                rvBottom.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvBottom.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvBottom.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                bottomMargin
            )
            constraintSet.constrainWidth(rvBottom.id, ConstraintSet.MATCH_CONSTRAINT)
        } else {
            // Restore mapContainer to the left (original XML layout)
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.END,
                rvCenter.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                0
            )
            constraintSet.constrainPercentWidth(mapContainer.id, 0.3f)

            // Set rvCenter to the right of mapContainer
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.END,
                rvRight.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvCenter.id, ConstraintSet.MATCH_CONSTRAINT)

            // Set rvRight to the right of rvCenter
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.constrainWidth(rvRight.id, ConstraintSet.WRAP_CONTENT)

            // Align tvCenterTitle with mapContainer
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin10sdp
            )
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                titleTopMargin
            )

            // Restore ivLogo to original position
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0
            )

            // Restore rvBottom to original position
            constraintSet.connect(
                rvBottom.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvBottom.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvBottom.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                bottomMargin
            )
            constraintSet.constrainWidth(rvBottom.id, ConstraintSet.MATCH_CONSTRAINT)
        }

        // Add smooth transition animation
        TransitionManager.beginDelayedTransition(root)

        // Apply the new constraints
        constraintSet.applyTo(root)
    }
}

fun Context.setUpMapPositionForReportingTemplate(binding: StampReportingTemplateLayoutBinding) {
    binding.apply {
        val constraintSet = ConstraintSet()
        constraintSet.clone(root)

        // Define margins (using your SDP resources)
        val sideMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)
        val titleTopMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp)
        val sideMargin10sdp = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        val logoMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._20sdp)
        val bottomMargin = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)

        val mapPosition = PrefManager.getInt(
            this@setUpMapPositionForReportingTemplate,
            Constants.SELECTED_MAP_POSITION + Constants.REPORTING_TEMPLATE,
            0
        )

        // Clear all relevant constraints first to avoid undefined references
        constraintSet.clear(mapContainer.id, ConstraintSet.START)
        constraintSet.clear(mapContainer.id, ConstraintSet.END)
        constraintSet.clear(rvCenter.id, ConstraintSet.START)
        constraintSet.clear(rvCenter.id, ConstraintSet.END)
        constraintSet.clear(rvRight.id, ConstraintSet.START)
        constraintSet.clear(rvRight.id, ConstraintSet.END)
        constraintSet.clear(tvCenterTitle.id, ConstraintSet.START)
        constraintSet.clear(ivLogo.id, ConstraintSet.END)

        if (mapPosition == 1) {
            // Set mapContainer to the far right
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.BOTTOM,
                rvBottom.id,
                ConstraintSet.TOP,
                bottomMargin
            )
            constraintSet.constrainPercentWidth(mapContainer.id, 0.3f)

            // Set rvRight to the left of mapContainer
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.constrainWidth(rvRight.id, ConstraintSet.WRAP_CONTENT)

            // Set rvCenter to the left of rvRight, filling remaining space
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.END,
                rvRight.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvCenter.id, ConstraintSet.MATCH_CONSTRAINT)

            // Align tvCenterTitle with rvCenter
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.START,
                titleTopMargin
            )
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0
            )

            // Position ivLogo to the left of mapContainer to avoid overlap
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.END,
                mapContainer.id,
                ConstraintSet.START,
                logoMargin
            )
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0
            )
        } else {
            // Restore mapContainer to the left (original XML layout)
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.END,
                rvCenter.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.connect(
                mapContainer.id,
                ConstraintSet.BOTTOM,
                rvBottom.id,
                ConstraintSet.TOP,
                bottomMargin
            )
            constraintSet.constrainPercentWidth(mapContainer.id, 0.3f)

            // Set rvCenter to the right of mapContainer
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvCenter.id,
                ConstraintSet.END,
                rvRight.id,
                ConstraintSet.START,
                sideMargin
            )
            constraintSet.constrainWidth(rvCenter.id, ConstraintSet.MATCH_CONSTRAINT)

            // Set rvRight to the right of rvCenter
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.START,
                rvCenter.id,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.connect(
                rvRight.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                sideMargin
            )
            constraintSet.constrainWidth(rvRight.id, ConstraintSet.WRAP_CONTENT)

            // Align tvCenterTitle with mapContainer
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.START,
                mapContainer.id,
                ConstraintSet.END,
                sideMargin10sdp
            )
            constraintSet.connect(
                tvCenterTitle.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                titleTopMargin
            )

            // Restore ivLogo to original position
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
            constraintSet.connect(
                ivLogo.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0
            )
        }

        // Add smooth transition animation
        TransitionManager.beginDelayedTransition(root)

        // Apply the new constraints
        constraintSet.applyTo(root)
    }
}


fun Context.getFontSizeFactor(passedTemplate: String): Float {
    return when (getInt(
        this@getFontSizeFactor,
        Constants.SELECTED_STAMP_SIZE + passedTemplate,
        0
    )) {
        0 -> 1.15f
        1 -> 1.05f
        2 -> 0.95f
        3 -> 0.85f
        else -> 1f
    }
}


fun ConstraintLayout.setStampPosition(stampPosition: StampCameraPosition) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)
    constraintSet.clear(R.id.stampContainer, ConstraintSet.BOTTOM)

    if (stampPosition == StampCameraPosition.TOP) {
        constraintSet.clear(R.id.overlayRootContainer, ConstraintSet.BOTTOM)
        constraintSet.connect(
            R.id.overlayRootContainer,
            ConstraintSet.TOP,
            R.id.previewContainer,
            ConstraintSet.TOP
        )
        constraintSet.setGoneMargin(
            R.id.overlayRootContainer,
            ConstraintSet.TOP,
            context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        )
    } else {
        constraintSet.clear(R.id.overlayRootContainer, ConstraintSet.TOP)
        constraintSet.connect(
            R.id.overlayRootContainer,
            ConstraintSet.BOTTOM,
            R.id.previewContainer,
            ConstraintSet.BOTTOM
        )
        // remove top margin
        constraintSet.setMargin(
            R.id.overlayRootContainer, ConstraintSet.BOTTOM,
            context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        )
    }

    val overlayRootContainer = findViewById<ConstraintLayout>(R.id.overlayRootContainer)
    val params = overlayRootContainer.layoutParams as ConstraintLayout.LayoutParams
    params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
    overlayRootContainer.layoutParams = params


    constraintSet.applyTo(this)
}

fun ConstraintLayout.setStampPositionForTopTemplate() {
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)

    constraintSet.clear(R.id.overlayRootContainer, ConstraintSet.BOTTOM)
    constraintSet.clear(R.id.overlayRootContainer, ConstraintSet.TOP)
    constraintSet.clear(R.id.stampContainer, ConstraintSet.BOTTOM)
    constraintSet.connect(
        R.id.overlayRootContainer,
        ConstraintSet.TOP,
        R.id.previewContainer,
        ConstraintSet.TOP
    )
    constraintSet.connect(
        R.id.overlayRootContainer,
        ConstraintSet.BOTTOM,
        R.id.previewContainer,
        ConstraintSet.BOTTOM
    )
    constraintSet.connect(
        R.id.stampContainer,
        ConstraintSet.BOTTOM,
        ConstraintSet.PARENT_ID,
        ConstraintSet.BOTTOM
    )

    val overlayRootContainer = findViewById<ConstraintLayout>(R.id.overlayRootContainer)

    val params = overlayRootContainer.layoutParams as ConstraintLayout.LayoutParams

    params.height = 0

    overlayRootContainer.layoutParams = params


    constraintSet.applyTo(this)
}






