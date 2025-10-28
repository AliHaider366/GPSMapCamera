package com.example.gpsmapcamera.utils


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.Window
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
            setCancelable(true)
        }

        setupViews()
        dialog?.show()
    }

    private fun setupViews() = with(binding) {
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvFeedback.text = when (rating.toInt()) {
                1 -> "Terrible"
                2 -> "Bad"
                3 -> "Okay"
                4 -> "Good"
                5 -> "Excellent"
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
