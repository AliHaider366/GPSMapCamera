package com.example.gpsmapcamera.utils

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.YuvImage
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.viewbinding.ViewBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.DropdownMenuAdapter
import com.example.gpsmapcamera.models.AddressLineModel
import com.example.gpsmapcamera.models.AddressModel
import com.example.gpsmapcamera.models.FullAddress
import com.example.gpsmapcamera.models.StampItemName
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.openlocationcode.OpenLocationCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume


fun EditText.addTextChanged(onTextChanged: ((String) -> Unit)? = null) {
    this.addTextChangedListener { editable ->
        val text = editable.toString()
        if (text.contains("_")) {
            val clean = text.replace("_", "")
            if (clean != text) {
                this.setText(clean)
                this.setSelection(clean.length) // keep cursor at end
            }
        }
        onTextChanged?.invoke(this.text.toString())
    }
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.setViewBackgroundDrawableRes(@DrawableRes drawableRes: Int) {
    background = ContextCompat.getDrawable(context, drawableRes)
}

fun View.setSelectionState(
    selectedCheckBox: CheckBox,
    vararg others: Pair<View, CheckBox>
) {
    // Set selected item
    setViewBackgroundDrawableRes(R.drawable.bg_language_item_selected)
    selectedCheckBox.isChecked = true

    // Set other items
    others.forEach { (view, checkbox) ->
        view.setViewBackgroundDrawableRes(R.drawable.bg_language_item)
        checkbox.isChecked = false
    }
}

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
    return String.format(
        "%d°%d'%s\"",
        degrees, minutes, String.format("%.2f", seconds)
    )
}

fun Pair<Double, Double>.toDMSPair(): Pair<String, String> {
    fun Double.toDMS(): String {
        val degrees = this.toInt()
        val minutesDecimal = (this - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val seconds = (minutesDecimal - minutes) * 60
        return String.format(
            "%d°%d'%.2f\"",
            degrees, minutes, seconds
        )
    }

    val latDMS = first.toDMS()
    val lonDMS = second.toDMS()
    return Pair(latDMS, lonDMS)
}

@Suppress("MissingPermission")
suspend fun Context.getCurrentLatLong(): Pair<Double, Double> {

    if (!isLocationEnabled()) {
        return 0.0 to 0.0 // instantly return if location is OFF
    }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    // First, try last known location
    val lastKnown = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location.latitude to location.longitude)
                } else {
                    cont.resume(null) // fallback
                }
            }
            .addOnFailureListener {
                cont.resume(null) // fallback
            }
    }

    // If lastKnown was available, return it
    if (lastKnown != null)
        return lastKnown

    // Otherwise request fresh GPS location
    return requestFreshLocation()
}

@Suppress("MissingPermission")
suspend fun Context.getCurrentAddress(): AddressLineModel {
    val (lat, lon) = getCurrentLatLong()

    if (lat == 0.0 && lon == 0.0) {
        return AddressLineModel("Unknown", "Unknown", "Unknown", "Unknown")
    }

    return try {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = withContext(Dispatchers.IO) {
            geocoder.getFromLocation(lat, lon, 1) // Get single address
        }

        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]

            val street = addr.getAddressLine(0) ?: "Unknown"
            val city = addr.locality ?: addr.subAdminArea ?: "Unknown"
            val province = addr.adminArea ?: "Unknown"
            val country = addr.countryName ?: "Unknown"

            AddressLineModel(street, city, province, country)
        } else {
            AddressLineModel("Unknown", "Unknown", "Unknown", "Unknown")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        AddressLineModel("Unknown", "Unknown", "Unknown", "Unknown")
    }
}


@Suppress("MissingPermission")
suspend fun Context.requestFreshLocation(): Pair<Double, Double> {
    if (!isLocationEnabled()) {
        return 0.0 to 0.0 // instantly return if location is OFF
    }

    return withTimeoutOrNull(5000) { // wait max 5s
        suspendCancellableCoroutine { cont ->
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this@requestFreshLocation)

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000
            ).setMaxUpdates(1).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    val fresh = result.lastLocation
                    if (fresh != null) {
                        cont.resume(fresh.latitude to fresh.longitude)
                    } else {
                        cont.resume(0.0 to 0.0)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            )

            cont.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    } ?: (0.0 to 0.0) // fallback if timeout
}

@Suppress("MissingPermission")
/*suspend fun Context.getCurrentPlusCode(): String =
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
    }*/
suspend fun Context.getCurrentPlusCode(): String {
    if (!isLocationEnabled()) {
        return ""// instantly return if location is OFF
    }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    // First, try last known location
    val lastKnown = suspendCancellableCoroutine<String?> { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(getPlusCode(location.latitude, location.longitude))
                } else {
                    cont.resume(null) // fallback
                }
            }
            .addOnFailureListener {
                cont.resume(null) // fallback
            }
    }

    if (lastKnown != null) return lastKnown

    /*   // Otherwise request fresh location
       val (lat, lng) = requestFreshLocation()
       return if (lat != 0.0 && lng != 0.0) {
           getPlusCode(lat, lng)
       } else {
           ""
       }*/
    // Try fresh location
    return try {
        val (lat, lng) = requestFreshLocation()
        if (lat != 0.0 && lng != 0.0) {
            getPlusCode(lat, lng)
        } else {
            "" // fallback when GPS off
        }
    } catch (e: Exception) {
        "" // ensure return
    }
}

inline fun <reified T : ViewBinding> Context.showCustomDialog(
    noinline bindingInflater: (LayoutInflater) -> T,
    isCancelable: Boolean = true,
    crossinline onBind: (binding: T, dialog: Dialog) -> Unit
) {
    val binding = bindingInflater(LayoutInflater.from(this))

    val dialog = Dialog(this).apply {
        setContentView(binding.root)
        setCancelable(isCancelable)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    onBind(binding, dialog)
    dialog.show()
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.openLatestImageFromFolder(folderPath: String) {
//    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val uri = MediaStore.Files.getContentUri("external")

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Images.Media.DATA,          // For Android < 10
        MediaStore.Images.Media.RELATIVE_PATH  // For Android 10+
    )

    val selection: String
    val selectionArgs: Array<String>

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ → use RELATIVE_PATH
//        selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
//        selectionArgs = arrayOf(folderPath)
        selection =
            "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND " +
                    "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)"
        selectionArgs = arrayOf(
            folderPath,
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
    } else {
        // Below Android 10 → use absolute DATA path
        val cameraPath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM
        ).toString() + "/" + folderPath.replace("DCIM/", "")
//        selection = "${MediaStore.Images.Media.DATA} LIKE ?"
//        selectionArgs = arrayOf("$cameraPath%")
        selection =
            "${MediaStore.Files.FileColumns.DATA} LIKE ? AND " +
                    "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)"
        selectionArgs = arrayOf(
            "$cameraPath%",
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
    }

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        if (cursor.moveToFirst()) {
//            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val type =
                cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))

//            val contentUri = ContentUris.withAppendedId(uri, id)
            val contentUri = when (type) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                else -> null
            }

            val mimeType =
                if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) "video/*" else "image/*"
            val baseIntent = Intent(Intent.ACTION_VIEW).apply {
//                setDataAndType(contentUri, "image/*")
                setDataAndType(contentUri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

//            startActivity(Intent.createChooser(baseIntent, "Complete action using Albums"))
            startActivity(baseIntent)

            /*  val pm = packageManager
              val resolvedApps = pm.queryIntentActivities(baseIntent, 0)

              val targetedIntents = mutableListOf<Intent>()
              for (res in resolvedApps) {
                  val packageName = res.activityInfo.packageName
                  val label = res.loadLabel(pm).toString().lowercase()

                  if (label.contains("gallery") ||  label.contains("photos")  || label.contains("album")) {
                      val targetedIntent = Intent(baseIntent)
                      targetedIntent.setPackage(packageName)
                      targetedIntents.add(targetedIntent)
                  }
              }

              if (targetedIntents.isNotEmpty()) {
                  val chooserIntent = Intent.createChooser(targetedIntents.removeAt(0), "Open with")
                  chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
                  startActivity(chooserIntent)
              } else {
                  Toast.makeText(this, "No supported gallery apps found", Toast.LENGTH_SHORT).show()
              }*/
            return
        }
    }

    Toast.makeText(this, "No images found in $folderPath", Toast.LENGTH_SHORT).show()
}

fun Context.shareImage(imageUri: Uri, message: String = "Check out this photo I just captured! 📸") {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_TEXT, message)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "Share Image")
    startActivity(chooser)
}

fun Context.shareApp(appName: String = "my app") {
    val packageName = this.packageName
    val shareText = "Check out $appName: https://play.google.com/store/apps/details?id=$packageName"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, appName)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    startActivity(Intent.createChooser(intent, "Share via"))
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


fun TextView.showDropdownMenu(
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    val displayMetrics = Resources.getSystem().displayMetrics
    val menuWidth = displayMetrics.widthPixels / 2

    val popupView = LayoutInflater.from(context).inflate(R.layout.dropdown_menu, null)
    val recyclerView = popupView.findViewById<RecyclerView>(R.id.dropdownRecycler)

    val popupWindow = PopupWindow(
        popupView,
        menuWidth,
        WindowManager.LayoutParams.WRAP_CONTENT,
        true
    )

    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = DropdownMenuAdapter(items) { selected ->
        text = selected           // update the TextView text
        onItemSelected(selected)  // return value to caller
        popupWindow.dismiss()
    }

    popupWindow.isOutsideTouchable = true
    popupWindow.elevation = 8f

    popupWindow.showAsDropDown(this)
}

fun TextView.setDrawable(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    val startDrawable = start?.let { ContextCompat.getDrawable(context, it) }
    val topDrawable = top?.let { ContextCompat.getDrawable(context, it) }
    val endDrawable = end?.let { ContextCompat.getDrawable(context, it) }
    val bottomDrawable = bottom?.let { ContextCompat.getDrawable(context, it) }

    setCompoundDrawablesWithIntrinsicBounds(
        startDrawable, topDrawable, endDrawable, bottomDrawable
    )
}

fun String.updateFileNameWithCurrentValues(
    newDateTime: String,
    newDay: String,
    newTimeZone: String? = null,
    newLatLong: String? = null
): String {
    val parts = this.removeSuffix(".jpg").split("_").toMutableList()

    // Regex patterns
    val date = Regex("\\d{8}")              // 20250831_154957
    val Time24h = Regex("\\d{6}")              // 20250831_154957
    val Time12h = Regex("\\d{6}(AM|PM)")       // 20250831_035521PM
    val dayRegex = Regex("(?i)(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)")
    val tzRegex = Regex("GMT[+-]\\d{2}:\\d{2}")

    // Decimal lat/long e.g. 33.6771516,72.6720808
    val latLongDecimal = Regex("-?\\d+\\.\\d+,-?\\d+\\.\\d+")
    // DMS format e.g. 33*40'37.75'',72*40'19.4''
    val latLongDMS = Regex("\\d+°\\d+'\\d+(\\.\\d+)?''[, ]\\d+°\\d+'\\d+(\\.\\d+)?''")

    for (i in parts.indices) {
        when {
            Time24h.matches(parts[i]) || Time12h.matches(parts[i]) -> parts[i] =
                newDateTime.split("_")[1]

            date.matches(parts[i]) -> parts[i] = newDateTime.split("_")[0]
            dayRegex.matches(parts[i]) -> parts[i] = newDay
//            tzRegex.matches(parts[i]) -> parts[i] = newTimeZone
//            latLongDecimal.matches(parts[i]) || latLongDMS.matches(parts[i]) -> parts[i] = newLatLong
        }
    }

    return parts.joinToString("_") + ".jpg"
}


fun TextView.enableMarquee() {
    isSelected = true
    isSingleLine = true
    ellipsize = TextUtils.TruncateAt.MARQUEE
    marqueeRepeatLimit = -1
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
    TextViewCompat.setCompoundDrawableTintList(
        this,
        ContextCompat.getColorStateList(context, colorResId)
    )
}

fun TextView.setTextColorAndBackgroundTint(textColorRes: Int, backgroundTintRes: Int) {
    // Change text color
    setTextColor(ContextCompat.getColor(context, textColorRes))
    // Change background tint
    ViewCompat.setBackgroundTintList(
        this,
        ColorStateList.valueOf(ContextCompat.getColor(context, backgroundTintRes))
    )
}

fun EditText.addAfterTextChanged(onChanged: (String) -> Unit): TextWatcher {
    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            onChanged(editable?.toString().orEmpty())
        }
    }
    this.addTextChangedListener(watcher)
    return watcher
}

fun TextView.setCompoundDrawableTintAndTextColor(
    drawableTintRes: Int? = null,
    textColorRes: Int? = null
) {
    drawableTintRes?.let {
        val tintColor = ContextCompat.getColor(context, it)
        compoundDrawablesRelative.forEach { drawable ->
            drawable?.setTint(tintColor)
        }
    }

    textColorRes?.let {
        setTextColor(ContextCompat.getColor(context, it))
    }
}

fun ImageView.setImage(@DrawableRes drawableRes: Int) {
    setImageResource(drawableRes)
}

fun ImageView.setTintColor(@ColorRes colorRes: Int) {
    val color = ContextCompat.getColor(context, colorRes)
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
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

// --- Extension: convert ImageProxy to Bitmap ---
fun ImageProxy.tooBitmap(): Bitmap? {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    if (!yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)) return null
    val imageBytes = out.toByteArray()
    var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Rotate bitmap according to ImageInfo
    val rotationMatrix = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()) }
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true)
    return bitmap
}

///// permissions

fun ComponentActivity.registerGpsResolutionLauncher(
    onEnabled: () -> Unit,
    onDenied: () -> Unit
) = registerForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        onEnabled()
    } else {
        onDenied()
    }
}

fun Context.isLocationEnabled(): Boolean {
    val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun Context.checkAndRequestGps(
    launcher: ActivityResultLauncher<IntentSenderRequest>
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 2000
    ).setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(1000)
        .build()

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    val client = LocationServices.getSettingsClient(this)
    val task = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        // Already enabled, do nothing
    }

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                val intentSenderRequest =
                    IntentSenderRequest.Builder(exception.resolution).build()
                launcher.launch(intentSenderRequest)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        }
    }
}

fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

// ✅ Check if a permission is granted
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

// ✅ Request a single permission with launcher
fun ActivityResultLauncher<String>.requestPermission(permission: String) {
    this.launch(permission)
}

// ✅ Extension to check multiple permissions
fun ComponentActivity.arePermissionsGranted(permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun ComponentActivity.registerPermissionLauncher(
    permission: String,
    onGranted: () -> Unit,
    onDenied: (permanentlyDenied: Boolean) -> Unit
): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onGranted()
        } else {
            val permanentlyDenied = !shouldShowRequestPermissionRationale(permission)
            onDenied(permanentlyDenied)
        }
    }
}

fun ComponentActivity.registerMultiplePermissionsLauncher(
    permissions: Array<String>,
    onGranted: () -> Unit,
    onDenied: (permanentlyDenied: Boolean) -> Unit
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result.any { it.value } // At least one permission granted?

        if (granted) {
            onGranted()
        } else {
            // check if user permanently denied all
            val permanentlyDenied = permissions.all { !shouldShowRequestPermissionRationale(it) }
            onDenied(permanentlyDenied)
        }
    }
}


//// animations
fun FrameLayout.playCurtainAnimation(duration: Long = 800) {
    val container = this

    val leftCurtain = View(context).apply {
        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        layoutParams = FrameLayout.LayoutParams(
            container.width / 2,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.START
        }
    }

    val rightCurtain = View(context).apply {
        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        layoutParams = FrameLayout.LayoutParams(
            container.width / 2,
            FrameLayout.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.END
        }
    }

    container.addView(leftCurtain)
    container.addView(rightCurtain)

    // Animate curtains opening
    leftCurtain.animate()
        .translationX(-container.width / 2f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction { container.removeView(leftCurtain) }
        .start()

    rightCurtain.animate()
        .translationX(container.width / 2f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction { container.removeView(rightCurtain) }
        .start()
}

fun FrameLayout.animateLightSweep() {
    val sweepView = object : View(context) {
        private val paint = Paint()

        init {
            // Vertical white gradient stripe
            paint.shader = LinearGradient(
                0f, 0f, 0f, 200f, // vertical gradient
                intArrayOf(
                    ContextCompat.getColor(context, android.R.color.transparent),
                    ContextCompat.getColor(context, android.R.color.white),
                    ContextCompat.getColor(context, android.R.color.transparent)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        override fun onDraw(canvas: android.graphics.Canvas) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }

    // Fill parent
    sweepView.layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    sweepView.alpha = 0.7f
    this.addView(sweepView)

    // Animate sweep vertically (top → bottom)
    val anim =
        ObjectAnimator.ofFloat(sweepView, "translationY", -height.toFloat(), height.toFloat())
    anim.duration = 900
    anim.start()

    anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@animateLightSweep.removeView(sweepView)
        }
    })
}

fun FrameLayout.animateRippleReveal(previewView: PreviewView) {
    val context = previewView.context
    val overlay = RippleOverlay(context, previewView.width)

    overlay.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    addView(overlay)

    overlay.startRipple { removeView(overlay) }
}

private class RippleOverlay(context: Context, widthP: Int) : View(context) {
    private var radius = 0f
    private var maxRadius = 0f
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = widthP * 0.3f // ripple thickness
        color = ContextCompat.getColor(context, R.color.rippleColor) // ripple color
    }

    fun startRipple(onEnd: () -> Unit) {
        post {
            maxRadius = Math.hypot(width.toDouble(), height.toDouble()).toFloat()

            val animator = ValueAnimator.ofFloat(0f, maxRadius).apply {
                duration = 1500
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    radius = it.animatedValue as Float
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onEnd()
                    }
                })
            }
            animator.start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }
}


fun StampItemName.getIcon(): Int {
    return when (this) {
        StampItemName.CONTACT_NO -> R.drawable.ic_template_phone
        StampItemName.SOUND_LEVEL -> R.drawable.ic_template_sound_level
        StampItemName.ALTITUDE -> R.drawable.ic_template_altitude
        StampItemName.ACCURACY -> R.drawable.ic_template_accuracy
        StampItemName.WEATHER -> R.drawable.ic_template_temperature
        StampItemName.COMPASS -> R.drawable.ic_template_compass
        StampItemName.MAGNETIC_FIELD -> R.drawable.ic_template_magnetic_field
        StampItemName.PRESSURE -> R.drawable.ic_template_pressure
        StampItemName.WIND -> R.drawable.ic_template_wind_speed
        StampItemName.HUMIDITY -> R.drawable.ic_template_humidity
        else -> R.drawable.capture_btn_ic
    }
}


fun Date.formatToString(format: String, locale: Locale = Locale.getDefault()): String {
    return SimpleDateFormat(format, locale).format(this)
}

fun Context.isMicrophonePermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}


fun Context.updateAddressPref(model: AddressModel, passedTemplate: String) {
    when (model.title) {
        getString(R.string.city) -> {
            PrefManager.setBoolean(
                this, Constants.FULL_ADDRESS_CITY + passedTemplate, model.isChecked
            )
        }

        getString(R.string.state) -> {
            PrefManager.setBoolean(
                this, Constants.FULL_ADDRESS_STATE + passedTemplate, model.isChecked
            )
        }

        getString(R.string.country) -> {
            PrefManager.setBoolean(
                this, Constants.FULL_ADDRESS_COUNTRY + passedTemplate, model.isChecked
            )
        }

        getString(R.string.pincode) -> {
            PrefManager.setBoolean(
                this, Constants.FULL_ADDRESS_PIN_CODE + passedTemplate, model.isChecked
            )
        }
    }
}

fun FullAddress.updateAddressWithVisibility(context: Context, template: String): String {
    val pinCodeVisibility =
        PrefManager.getBoolean(context, Constants.FULL_ADDRESS_PIN_CODE + template, true)
    val stateVisibility =
        PrefManager.getBoolean(context, Constants.FULL_ADDRESS_STATE + template, true)
    val cityVisibility =
        PrefManager.getBoolean(context, Constants.FULL_ADDRESS_CITY + template, true)
    val countryVisibility =
        PrefManager.getBoolean(context, Constants.FULL_ADDRESS_COUNTRY + template, true)

    return buildString {
//        if (pinCodeVisibility) {
//            append(pinCode)
//            append(" ")
//        }
//        append(locality)
//        append(" ")

        if (locality.isNotEmpty()) {
            append(locality)
            append(", ")
        }
        if (cityVisibility) {
            if (city.isNotEmpty()) {
                append(city)
            } else {
                append(city)
            }
            append(", ")
        }
        if (stateVisibility) {
            append(state)
            append(", ")
        }
        if (countryVisibility) {
            append(country)
        }
    }

}


fun Int.saveSelectedMapType(): String {
    return when (this) {
        0 -> Constants.MAP_TYPE_NORMAL
        1 -> Constants.MAP_TYPE_SATELLITE
        2 -> Constants.MAP_TYPE_TERRAIN
        3 -> Constants.MAP_TYPE_HYBRID
        else -> Constants.MAP_TYPE_NORMAL
    }
}

fun Context.getSelectedMapDrawable(passedTemplate: String): Int {
    return when (PrefManager.getString(
        this, Constants.SELECTED_MAP_TYPE + passedTemplate,
        Constants.MAP_TYPE_NORMAL
    )) {
        Constants.MAP_TYPE_NORMAL -> R.drawable.map_type_normal
        Constants.MAP_TYPE_SATELLITE -> R.drawable.map_type_satellite
        Constants.MAP_TYPE_TERRAIN -> R.drawable.map_type_terrain
        Constants.MAP_TYPE_HYBRID -> R.drawable.map_type_hybrid
        else -> R.drawable.map_type_normal
    }
}

fun String.selectedMapToInt(): Int {
    return when (this) {
        Constants.MAP_TYPE_NORMAL -> 0
        Constants.MAP_TYPE_SATELLITE -> 1
        Constants.MAP_TYPE_TERRAIN -> 2
        Constants.MAP_TYPE_HYBRID -> 3
        else -> 0
    }
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this) // remove after first trigger
        }
    })
}



fun FrameLayout.loadStaticMap(
    context: Context,
    location: Location,
    template: String
) {
    val lat = location.latitude
    val lng = location.longitude
    val zoom = PrefManager.getFloat(
        context,
        Constants.SELECTED_MAP_ZOOM_LEVEL + template,
        12.5f
    )
    val mapType = PrefManager.getString(
        context,
        Constants.SELECTED_MAP_TYPE + template,
        Constants.MAP_TYPE_NORMAL
    )

    val typeParam = when (mapType) {
        Constants.MAP_TYPE_NORMAL -> "roadmap"
        Constants.MAP_TYPE_SATELLITE -> "satellite"
        Constants.MAP_TYPE_TERRAIN -> "terrain"
        Constants.MAP_TYPE_HYBRID -> "hybrid"
        else -> "roadmap"
    }

    Log.d("TAG", "loadStaticMap: $zoom")

    val apiKey = "AIzaSyB9bZ09nESdvT2kRmFEAKbQ3gUqJJwOApI" // your API key

    val url = "https://maps.googleapis.com/maps/api/staticmap" +
            "?center=$lat,$lng" +
            "&zoom=${zoom.toInt()}" +
            "&size=640x640" +        // max allowed
            "&scale=2" +             // doubles to 1280x1280 (HD)
            "&maptype=$typeParam" +
            "&markers=color:red%7C$lat,$lng" +
            "&key=$apiKey"


    this.removeAllViews()
    val imageView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }
    Glide.with(context).load(url).into(imageView)
    this.addView(imageView)
}

fun Activity.setPortraitOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

fun String.toLanguageName(): String {
    return when (this.lowercase()) {
        "en", "en-us" -> "English"
        "en-gb" -> "English (UK)"
        "fr", "fr-fr" -> "Français"
        "fr-ca" -> "Français (Canada)"
        "es", "es-es" -> "Español"
        "de", "de-de" -> "Deutsch"
        "zh", "zh-cn" -> "中文 (Chinese)"
        "hi", "hi-in" -> "हिन्दी"
        "pt", "pt-pt" -> "Português (Portugal)"
        "pt-br" -> "Português (Brazil)"
        "ru", "ru-ru" -> "Русский"
        "in", "id" -> "Indonesian"
        "fil", "ph" -> "Tagalog (Philippines)"
        "bn", "bn-bd" -> "বাংলা"
        "af" -> "Afrikaans"
        "ko", "ko-kr" -> "한국어 (Korean)"
        "nl", "nl-nl" -> "Nederlands"
        else -> this // fallback if not matched
    }
}

private var lastClickTime = 0L
private const val CLICK_DELAY = 500L // milliseconds

fun isSingleTouch(): Boolean {
    val currentTime = System.currentTimeMillis()
    return if (currentTime - lastClickTime > CLICK_DELAY) {
        lastClickTime = currentTime
        true
    } else {
        false
    }
}



