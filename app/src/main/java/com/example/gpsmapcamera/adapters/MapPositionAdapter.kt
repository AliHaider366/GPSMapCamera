package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleDateFormatBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.formatToString
import java.util.Date

class MapPositionAdapter(
    private val fromStampPosition: Boolean,
    private val dataList: List<String>,
    private val passedTemplate : String,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MapPositionAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0


    inner class ViewHolder(private val binding: SingleDateFormatBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            selectedPosition = getInt(binding.root.context, if(fromStampPosition) Constants.SELECTED_STAMP_POSITION + passedTemplate else Constants.SELECTED_MAP_POSITION + passedTemplate, 0)
        }
        fun bind(text: String, position: Int) = binding.run {
            tvMain.text = text

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