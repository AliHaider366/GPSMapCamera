package com.example.gpsmapcamera.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
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
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.example.gpsmapcamera.R
import com.google.android.gms.location.LocationServices
import com.google.openlocationcode.OpenLocationCode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
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

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
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
    val anim = ObjectAnimator.ofFloat(sweepView, "translationY", -height.toFloat(), height.toFloat())
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
    val overlay = RippleOverlay(context,previewView.width)

    overlay.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    addView(overlay)

    overlay.startRipple { removeView(overlay) }
}

private class RippleOverlay(context: Context,widthP:Int) : View(context) {
    private var radius = 0f
    private var maxRadius = 0f
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = widthP*0.3f // ripple thickness
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