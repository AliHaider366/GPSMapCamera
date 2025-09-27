package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.databinding.OnboardingItemLayoutBinding

class OnBoardingAdapter(private val itemsList: List<Pair<String, Int>>) : RecyclerView.Adapter<OnBoardingAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: OnboardingItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<String, Int>) {
            Glide.with(binding.imageView.context)
                .load(item.second)
                .into(binding.imageView)
            binding.titleTv.text=item.first
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = OnboardingItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(itemsList[position])
    }

    override fun getItemCount(): Int = itemsList.size
}
