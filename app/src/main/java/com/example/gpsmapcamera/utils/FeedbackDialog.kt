package com.example.gpsmapcamera.utils


import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.DialogFeedbackBinding

class FeedbackDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private lateinit var binding: DialogFeedbackBinding

    fun show() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            binding = DialogFeedbackBinding.inflate(LayoutInflater.from(context))
            setContentView(binding.root)
            window?.setBackgroundDrawableResource(R.color.transparent)
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

        tvSubmit.setOnClickListener {
            if (etFeedback.text?.isNotEmpty() == true) {
                context.openEmailApp(etFeedback.text.toString())
            }else{
                Toast.makeText(context, context.getString(R.string.please_enter_feedback), Toast.LENGTH_SHORT).show()
            }
            dialog?.dismiss()
        }

        tvCancel.setOnClickListener { dialog?.dismiss() }
    }
}
