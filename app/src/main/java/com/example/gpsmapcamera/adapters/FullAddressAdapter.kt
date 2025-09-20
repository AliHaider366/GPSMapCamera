package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.SingleFullAddressBinding
import com.example.gpsmapcamera.models.AddressModel

class FullAddressAdapter(
    private val dataList: List<AddressModel>,
    private val onItemClick: (AddressModel) -> Unit
) : RecyclerView.Adapter<FullAddressAdapter.ViewHolder>() {


    inner class ViewHolder(private val binding: SingleFullAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: AddressModel) = binding.run {

            tvMain.text = model.title

            btnSwitch.isChecked = model.isChecked

            btnSwitch.setOnCheckedChangeListener { _, isChecked ->
                model.isChecked = isChecked
                onItemClick(model)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SingleFullAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size
}