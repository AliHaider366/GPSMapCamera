package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleLatLongBinding
import com.example.gpsmapcamera.models.DynamicStampValues
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.formatCoordinatesByPosition
import com.example.gpsmapcamera.utils.formatPlusCodeByPosition

class LatLongAdapter(
    private val isFromPlusCode: Boolean,
    private val dataList: List<String>,
    private val passedTemplate: String,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<LatLongAdapter.ViewHolder>() {


    private var selectedPosition: Int = 0


    inner class ViewHolder(private val binding: SingleLatLongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var dynamicValues : DynamicStampValues?= null
        init {
            dynamicValues = (binding.root.context.applicationContext as MyApp).appViewModel.dynamicValues.value
            selectedPosition = getInt(binding.root.context, if(isFromPlusCode) Constants.SELECTED_PLUS_CODE + passedTemplate else Constants.SELECTED_LAT_LONG + passedTemplate , 0)
        }
        fun bind(item: String,position: Int) = binding.run {

            tvMain.text = item
            if (isFromPlusCode){
                tvDesc.text = dynamicValues?.latLong?.formatPlusCodeByPosition(position)?:""
            }else{
                tvDesc.text = dynamicValues?.latLong?.formatCoordinatesByPosition(position)?:""
            }


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
            SingleLatLongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position],position)
    }

    override fun getItemCount(): Int = dataList.size
}