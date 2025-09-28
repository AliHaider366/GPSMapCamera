package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.ItemDropdownMenuBinding

class DropdownMenuAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DropdownMenuAdapter.ViewHolder>() {



    inner class ViewHolder(private val binding: ItemDropdownMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {

            binding.apply {
                dropdownItemText.text = item
                dropdownItemText.setOnClickListener { onItemClick(item) }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDropdownMenuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}
