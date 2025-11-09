package com.example.gpsmapcamera.colorPicker

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.DialogColorPickerBinding
import com.example.gpsmapcamera.utils.showToast
import com.example.gpstest.ColorSliderView
import com.example.gpstest.HSVColorPickerView

class ColorPickerDialog(
    context: Context,
    private val initialColor: Int = Color.parseColor("#6366F1"),
    private val onApply: (Int) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogColorPickerBinding
    private var hue: Float = 0f
    private var saturation: Float = 0f
    private var value: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogColorPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT
        )

        val hsv = FloatArray(3)
        Color.colorToHSV(initialColor, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]

        setupViews()
    }

    private fun setupViews() = binding.apply {
        title.text = context.getString(R.string.app_name).let { _ -> "Color Picker" }

        // Initialize picker view
        pickerView.setSV(saturation, value)
        pickerView.setHue(hue)
        pickerView.listener = object : HSVColorPickerView.OnColorChangedListener {
            override fun onColorChanged(color: Int, h: Float, s: Float, v: Float) {
                hue = h
                saturation = s
                value = v
//                updatePreviewCircle(color)
                updateFields(color)
            }
        }

        // Custom hue slider (0..360)
        hueSeek.setMode(ColorSliderView.Mode.HUE)
        hueSeek.setValue(hue)
        hueSeek.listener = object : ColorSliderView.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                hue = value
                pickerView.setHue(hue)
                saturationSeek.setHue(hue)
//                updatePreviewCircle(currentColor())
            }
        }

        // Saturation slider (0..1) with hue-based gradient
        saturationSeek.setMode(ColorSliderView.Mode.SATURATION)
        saturationSeek.setHue(hue)
        saturationSeek.setValue(saturation)
        saturationSeek.listener = object : ColorSliderView.OnValueChangedListener {
            override fun onValueChanged(value: Float) {
                saturation = value
                pickerView.setSV(saturation, this@ColorPickerDialog.value)
//                updatePreviewCircle(currentColor())
                updateFields(currentColor())
            }
        }

        // Hex and RGB
        updateFields(currentColor())
//        updatePreviewCircle(currentColor())

        btnCopy.setOnClickListener {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("hex", hexInput.text?.toString() ?: ""))
        }

        btnApply.setOnClickListener {
            onApply(currentColor())
            dismiss()
        }

        backBtn.setOnClickListener { dismiss() }
    }

    private fun currentColor(): Int = Color.HSVToColor(floatArrayOf(hue, saturation, value))

    // Gradients are drawn by the custom ColorSliderView

//    private fun updatePreviewCircle(color: Int) = binding.apply { previewCircle.setColorFilter(color) }

    private fun updateFields(color: Int) = binding.apply {
        val hex = String.format("#%06X", 0xFFFFFF and color)
        hexInput.setText(hex)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        rInput.setText(r.toString())
        gInput.setText(g.toString())
        bInput.setText(b.toString())
    }
}


