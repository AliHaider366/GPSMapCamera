package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.databinding.StartMenu4ItemLayoutBinding
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.visible

class StartMenu4Adapter(private val itemsList: List<Pair<String, Int>>,val onSelected: (Int,String)->Unit) : RecyclerView.Adapter<StartMenu4Adapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: StartMenu4ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        private var selectedPos=-1
        fun bind(item: Pair<String, Int>) {
            Glide.with(binding.imageView.context)
                .load(item.second)
                .into(binding.imageView)
            binding.titleTv.text=item.first

            if (selectedPos==position)
                binding.selectionIc.visible()
            else
                binding.selectionIc.gone()

            binding.root.setOnClickListener{
                selectedPos=position
                onSelected(position,item.first)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = StartMenu4ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(itemsList[position])
    }

    override fun getItemCount(): Int = itemsList.size
}
