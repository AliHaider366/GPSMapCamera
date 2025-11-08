package com.example.gpsmapcamera.utils


import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.DialogConfirmDeleteBinding

class DeleteConfirmationDialog(
    private val context: Context,
    private val onDeleteClick: () -> Unit
) {

    private var dialog: Dialog? = null
    private lateinit var binding: DialogConfirmDeleteBinding

    fun show() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            binding = DialogConfirmDeleteBinding.inflate(LayoutInflater.from(context))
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
        dialog?.show()

        binding.tvDelete.setOnClickListener {
            onDeleteClick()
            dialog?.dismiss()
        }

        binding.ivCross.setOnClickListener { dialog?.dismiss() }

    }


}
