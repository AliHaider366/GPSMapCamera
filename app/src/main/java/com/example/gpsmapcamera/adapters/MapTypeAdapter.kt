package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleDateFormatBinding
import com.example.gpsmapcamera.databinding.SingleMapTypeBinding
import com.example.gpsmapcamera.models.MapTypeModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.PrefManager.getString
import com.example.gpsmapcamera.utils.selectedMapToInt

class MapTypeAdapter(
    private val mapTypeList: List<MapTypeModel>,
    private val passedTemplate: String,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MapTypeAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0


    inner class ViewHolder(private val binding: SingleMapTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            selectedPosition = getString(
                binding.root.context,
                Constants.SELECTED_MAP_TYPE + passedTemplate,
                Constants.MAP_TYPE_NORMAL
            ).selectedMapToInt()
        }

        fun bind(model: MapTypeModel, position: Int) = binding.run {
            tvMain.text = model.title
            ivMain.setImageResource(model.icon)

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
        val binding =
            SingleMapTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mapTypeList[position], position)
    }

    override fun getItemCount(): Int = mapTypeList.size
}
