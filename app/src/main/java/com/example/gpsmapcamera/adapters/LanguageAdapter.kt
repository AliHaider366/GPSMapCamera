package com.example.gpsmapcamera.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.ItemLanguageBinding
import com.example.gpsmapcamera.utils.enableMarquee
import com.example.gpsmapcamera.utils.setViewBackgroundDrawableRes
import com.example.mycam.models.Language
import com.murgupluoglu.flagkit.FlagKit
import kotlin.math.log

class LanguageAdapter(
    private val onLanguageSelected: (Language) -> Unit
) : ListAdapter<Language, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSelection(selectedLanguage: Language) {
        val updatedList = currentList.map { language ->
            language.copy(isSelected = language.code == selectedLanguage.code)
        }
        Log.d("TAG", "setupRecyclerView: $updatedList")
        submitList(updatedList)
    }

   inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(language: Language) {
            with(binding) {
                // Set language data
                tvLanguageName.text = language.name
                tvNativeName.text = language.nativeName
                tvLanguageName.enableMarquee()
                tvNativeName.enableMarquee()

                // Set flag using FlagKit
                setupFlagWithFlagKit(language)

                updateSelectionUI(language.isSelected)

                // Set click listener
                root.setOnClickListener {

                    onLanguageSelected(language)
                }
            }
        }

        private fun updateSelectionUI(isSelected: Boolean) {
            with(binding) {
                if (isSelected) {
                    root.setViewBackgroundDrawableRes(R.drawable.bg_language_item_selected)

                } else {
                    root.setViewBackgroundDrawableRes(R.drawable.bg_language_item)


                }
            }
        }

        private fun setupFlagWithFlagKit(language: Language) {
            val context = binding.root.context

            // Get flag drawable from FlagKit
            val flagDrawable = getFlag(context, language.countryCode)

            if (flagDrawable != null) {
                Glide.with(context).load(flagDrawable).into(binding.ivFlag)
            } else {
                // Fallback to placeholder
                Glide.with(context).load(R.drawable.ic_launcher_background).into(binding.ivFlag)
            }
        }

        fun getFlag(context: Context, countryCode: String): Drawable? {
            return try {
                FlagKit.getDrawable(context, countryCode.uppercase())
            } catch (e: Exception) {
                // Return default flag or null
                ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
            }
        }

    }
}


class LanguageDiffCallback : DiffUtil.ItemCallback<Language>() {
    override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem.code == newItem.code
    }

    override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
        return oldItem == newItem
    }
}
