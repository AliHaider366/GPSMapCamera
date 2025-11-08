package com.example.gpsmapcamera.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleDateFormatBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.stampFontList

class StampFontAdapter(
    private val dataList: List<Int?>,
    private val passedTemplate : String,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<StampFontAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0


    inner class ViewHolder(private val binding: SingleDateFormatBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            selectedPosition = getInt(binding.root.context, Constants.SELECTED_STAMP_FONT + passedTemplate, 0)
        }
        fun bind(fontResId: Int?, position: Int) = binding.run {
            tvMain.text = "GPS Stamp Camera - Photo Stamp"

            // Apply the downloadable font
            val typeface = if (fontResId != null) {
                ResourcesCompat.getFont(root.context, fontResId)
            } else {
                Typeface.DEFAULT // system default font
            }
            tvMain.typeface = typeface

            // Update background based on selection
            if (position == selectedPosition) {
                root.setBackgroundResource(R.drawable.bg_selected_stroke_5)
                tvMain.setTextColor(root.context.getColor(R.color.blue))
            } else {
                root.setBackgroundResource(R.drawable.bg_white_stroke_5)
                tvMain.setTextColor(root.context.getColor(R.color.textMainColor))
            }

            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition) // update old
                notifyItemChanged(selectedPosition) // update new
                onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleDateFormatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position], position)
    }

    override fun getItemCount(): Int = dataList.size
}