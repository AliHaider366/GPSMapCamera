package com.example.gpsmapcamera.adapters

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.databinding.ItemManualLocationBinding
import com.example.gpsmapcamera.models.ManualLocation
import com.example.gpsmapcamera.utils.MyApp
import java.util.Locale

class ManualLocationAdapter(
    private val locations: MutableList<ManualLocation>,
    private val onLocationSelected: (ManualLocation, Boolean) -> Unit
) : RecyclerView.Adapter<ManualLocationAdapter.ManualLocationViewHolder>() {

    var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManualLocationViewHolder {
        val binding = ItemManualLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ManualLocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManualLocationViewHolder, position: Int) {
        holder.bind(locations[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = locations.size

    fun addLocation(location: ManualLocation) {
        locations.add(location)
        notifyItemInserted(locations.size - 1)
    }

    fun clearSelection() {
        val previousSelected = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        if (previousSelected != RecyclerView.NO_POSITION && previousSelected in 0 until locations.size) {
            notifyItemChanged(previousSelected)
        }
    }

    fun removeLocation(position: Int) {
        if (position in 0 until locations.size) {
            locations.removeAt(position)
            notifyItemRemoved(position)
            if (selectedPosition == position) {
                selectedPosition = RecyclerView.NO_POSITION
            } else if (selectedPosition > position) {
                selectedPosition--
            }
        }
    }

    inner class ManualLocationViewHolder(
        private val binding: ItemManualLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: ManualLocation, isSelected: Boolean) {
            val context=  binding.root.context.applicationContext as MyApp
            binding.tvTitle.text = location.title
            binding.tvAddressLine1.text = location.address
            binding.tvAddressLine2.text = "${location.city}, ${location.state}"
            binding.tvCity.text = location.city
            binding.tvLatLong.text = String.format(
                Locale.US,
                "Lat %.7f long %.7f",
                location.latitude,
                location.longitude
            )
            binding.cbLocation.isChecked = isSelected

            // Load static map image
            val staticMapUrl = generateStaticMapUrl(context,location.latitude, location.longitude)
            Glide.with(binding.root.context)
                .load(staticMapUrl)
                .into(binding.ivMapThumbnail)

            // Remove previous listeners to avoid conflicts
            binding.cbLocation.setOnCheckedChangeListener(null)
            binding.cbLocation.isChecked = isSelected

            // Handle checkbox click
            binding.cbLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val previousSelected = this@ManualLocationAdapter.selectedPosition
                    this@ManualLocationAdapter.selectedPosition = adapterPosition
                    if (previousSelected != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelected)
                    }
                    notifyItemChanged(adapterPosition)
                    onLocationSelected(location, true)
                } else {
                    if (this@ManualLocationAdapter.selectedPosition == adapterPosition) {
                        this@ManualLocationAdapter.selectedPosition = RecyclerView.NO_POSITION
                        onLocationSelected(location, false)
                    }
                }
            }

            // Handle card click
            binding.root.setOnClickListener {
                binding.cbLocation.isChecked = !binding.cbLocation.isChecked
            }
        }

        private fun generateStaticMapUrl(appContext: Application,latitude: Double, longitude: Double): String {
            return "https://maps.googleapis.com/maps/api/staticmap?" +
                    "center=$latitude,$longitude" +
                    "&zoom=15" +
                    "&size=180x180" +
                    "&markers=color:red%7C$latitude,$longitude" +
                    "&key=${(appContext as MyApp).mapApiKey}"
        }
    }
}

