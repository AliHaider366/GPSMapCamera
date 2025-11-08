package com.example.gpsmapcamera.utils


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.DialogRateUsBinding

class RateUsDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private lateinit var binding: DialogRateUsBinding

    fun show() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            binding = DialogRateUsBinding.inflate(LayoutInflater.from(context))
            setContentView(binding.root)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(window?.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT

            window?.attributes = lp
            window?.setGravity(Gravity.CENTER)
            window?.setDimAmount(0.7f)
            setCancelable(true)
        }

        setupViews()
        dialog?.show()
    }

    private fun setupViews() = with(binding) {
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvFeedback.text = when (rating.toInt()) {
                1 -> context.getString(R.string.terrible)
                2 -> context.getString(R.string.bad)
                3 -> context.getString(R.string.okay)
                4 -> context.getString(R.string.good)
                5 -> context.getString(R.string.excellent)
                else -> ""
            }
        }

        btnRateNow.setOnClickListener {
            val appPackageName = context.packageName
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (e: Exception) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
            dialog?.dismiss()
        }

        tvLater.setOnClickListener { dialog?.dismiss() }
    }
}
